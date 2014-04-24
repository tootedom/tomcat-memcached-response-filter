package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import org.greencheek.web.filter.memcached.cachekey.CacheKey;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.config.MemcachedFetchingConfig;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.domain.CachedResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dominictootell on 16/04/2014.
 */
public class SpyFilterMemcachedFetching implements FilterMemcachedFetching {

    private final MemcachedClient client;
    private final MemcachedFetchingConfig config;

    public SpyFilterMemcachedFetching(MemcachedClient client, MemcachedFetchingConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public CachedResponse getCachedContent(HttpServletRequest theRequest) {
        String cacheControlHeader = theRequest.getHeader(CacheConfigGlobals.CACHE_CONTROL_HEADER);
        if(cacheControlHeader != null && hasClientCacheBuster(config.getNoCacheHeaders(),cacheControlHeader)) {
            return CachedResponse.MISS;
        } else {
            String key = config.getKeyConfig().createCacheKey(theRequest);
            if(key==null) return CachedResponse.MISS;

            byte[] content = get(key);
            if(content == null) {
                return CachedResponse.MISS;
            } else {
                return parseCachedResponse(content);
            }
        }
    }

    private boolean hasClientCacheBuster(String[] values,String headerValues) {
        if(values.length == 0) return false;
        for(String val : values) {
            if(headerValues.contains(val)) return true;
        }
        return false;
    }

    private byte[] get(String key) {
        GetFuture<Object> future = client.asyncGet(key);

        try {
            Object o = future.get(config.getCacheGetTimeoutInMillis(), TimeUnit.MILLISECONDS);
            return (byte[])o;
        } catch(Exception e) {
            return null;
        }
    }

    private int parseStatusCode(byte[] content,int offset, int length) {
        int finalPosition = offset + length;
        for(int i = 0;i < length;i++) {
            if(content[offset++]!=32) continue;
            else break;
        }

        while(offset<finalPosition) {
            if(content[offset] > 47 && content[offset] < 58) break;
            else offset++;
        }


        if(offset+3<finalPosition) {
            char[] status = new char[3];
            for(int i=0;i<3;i++) {
                status[i] = (char)content[offset+i];
            }
            try {
                return Integer.parseInt(new String(status));
            } catch(NumberFormatException e) {
                return 200;
            }
        } else {
            return 200;
        }
    }

    public CachedResponse parseCachedResponse(byte[] content) {
        Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>();
        int offset = 0;
        byte prevMinOne = -1;
        byte prevMinTwo = -1;
        byte prev = -1;
        int colon = -1;
        int statusCode = 200;
        for(int i=0;i<content.length;i++) {
            if(content[i] == 58 && colon==-1) {
                colon = i+2;
                continue;
            }

            if((content[i] ^ CacheConfigGlobals.NEW_LINE[1]) * (prev ^ CacheConfigGlobals.NEW_LINE[0] ) == 0) {
                if((prevMinTwo ^ CacheConfigGlobals.NEW_LINE[0]) * (prevMinOne ^ CacheConfigGlobals.NEW_LINE[1]) == 0){
                    offset = i+2;
                    break;
                } else {
                    if (colon != -1) {
                        String key = toString(content, offset, colon - 2 - offset);
                        String value = toString(content, colon, (i - 1 - colon));
                        Collection<String> existing = headers.get(key);
                        if(existing==null) {
                            existing = new ArrayList<String>(1);
                            headers.put(key,existing);
                        }
                        existing.add(value);

                    } else {
                        statusCode = parseStatusCode(content,offset,colon-2);
                    }

                    offset = i+1;
                    colon = -1;
                }
            }

            prevMinTwo = prevMinOne;
            prevMinOne = prev;
            prev = content[i];
        }

        if(headers.size()>0) {
            return new CachedResponse(true,statusCode, headers, content, offset-1);
        } else {
            return CachedResponse.MISS;
        }
    }

    public String toString(byte[] content, int start, int end) {
        try {
            return new String(content,start,end,"UTF-8");
        } catch(UnsupportedEncodingException e) {
            return new String(content,start,end);
        }
    }
}
