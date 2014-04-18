package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.MemcachedFetchingConfig;
import org.greencheek.web.filter.memcached.client.MemcachedStorageConfig;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.domain.CachedResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
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
        if(cacheControlHeader == null || cacheControlHeader.contains(CacheConfigGlobals.NO_CACHE_CLIENT_VALUE)) {
            return CachedResponse.MISS;
        } else {
            String key = config.getKeyConfig().createCacheKey(theRequest);
            byte[] content = get(key);
            if(content == null) {
                return CachedResponse.MISS;
            } else {
                return parseCachedResponse(content);
            }
        }
    }

    public byte[] get(String key) {
        GetFuture<Object> future = client.asyncGet(key);

        try {
            Object o = future.get(config.getCacheGetTimeoutInMillis(), TimeUnit.MILLISECONDS);
            return (byte[])o;
        } catch(Exception e) {
            return null;
        }
    }

    public CachedResponse parseCachedResponse(byte[] content) {
        Map<String,String> headers = new HashMap<String,String>();
        int offset = 0;
        byte prevMinOne = -1;
        byte prevMinTwo = -1;
        byte prev = -1;
        int colon = -1;
        for(int i=0;i<content.length;i++) {
            if(content[i] == 58) {
                colon = i+2;
                continue;
            }

            if((content[i] ^ CacheConfigGlobals.NEW_LINE[1]) * (prev ^ CacheConfigGlobals.NEW_LINE[0] ) == 0) {
                if((prevMinTwo ^ CacheConfigGlobals.NEW_LINE[0]) * (prevMinOne ^ CacheConfigGlobals.NEW_LINE[1]) == 0){
                    offset = i+2;
                    break;
                } else {
                    String key = toString(content, offset, colon-2 - offset);
                    String value = toString(content, colon, (i-1 - colon));
                    headers.put(key, value);
                    offset = i+1;
                }
            }

            prevMinTwo = prevMinOne;
            prevMinOne = prev;
            prev = content[i];
        }

        if(headers.size()>0) {
            return new CachedResponse(true, headers, content, offset);
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
