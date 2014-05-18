package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.GetFuture;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.config.MemcachedFetchingConfig;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by dominictootell on 16/04/2014.
 */
public class SpyFilterMemcachedFetching implements FilterMemcachedFetching {

    private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedFetching.class);


    private static final byte COLON = 58;
    private static final byte SPACE = 32;
    private static final byte ZERO = 48;

    private final MemcachedClient client;
    private final MemcachedFetchingConfig config;

    public SpyFilterMemcachedFetching(MemcachedClient client, MemcachedFetchingConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public CachedResponse getCachedContent(String key) {
        return parseBytes(get(key));
    }

    @Override
    public Map<String, CachedResponse> getCachedContent(Collection<String> keys) {
        Map<String,Object> responses = multiGet(keys);
        if(responses == null || responses.size()==0) return allMissing(keys);

        Map<String,CachedResponse> parsedResponses = new HashMap<String,CachedResponse>(responses.size(),1.0f);

        for(String key : keys) {
            Object o = responses.get(key);
            parsedResponses.put(key,parseBytes(o));
        }
        return parsedResponses;
    }

    public Map<String,CachedResponse> allMissing(Collection<String> keys) {
        Map<String,CachedResponse> missingMap = new HashMap<String,CachedResponse>(keys.size(),1.0f);

        for(String key : keys) {
            missingMap.put(key,CachedResponse.MISS);
        }
        return missingMap;
    }

    private CachedResponse parseBytes(Object o) {
        if(o==null) return CachedResponse.MISS;

        try {
            byte[] content = (byte[])o;
            return parseCachedResponse(content);
        } catch (ClassCastException e) {
            return CachedResponse.MISS;
        }
    }

    private Object get(String key) {
        GetFuture<Object> future = client.asyncGet(key);
        try {
            return future.get(config.getCacheGetTimeoutInMillis(), TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            log.warn("{\"method\":\"get\",\"exception\":\"{}\",\"message\":\"Exception whilst attempting get from memcached for key\",\"key\":\"{}\"}",e.getMessage(),key);
            return null;
        }
    }

    private Map<String,Object> multiGet(Collection<String> keys) {
        BulkFuture<Map<String,Object>> future = client.asyncGetBulk(keys);
        try {
            return future.get(config.getCacheGetTimeoutInMillis(), TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            log.warn("{\"method\":\"multiGet\",\"exception\":\"{}\",\"message\":\"Exception whilst attempting get from memcached for keys\",\"keys\":\"{}\"}",e.getMessage(),keys);
            return null;
        }
    }

    /**
     * returns twos ints, the first is the status code
     * the seconds is the end of the status code line
     *
     * @param content
     * @return
     */
    private int[] parseStatusCode(byte[] content) {
        int i =0;
        byte prev = -1;
        while(i<content.length) {
            if( (content[i] == CacheConfigGlobals.NEW_LINE[1]) && (prev == CacheConfigGlobals.NEW_LINE[0]) ) {
                break;
            }
            prev = content[i++];
        }

        return new int[]{parseStatusCode(content,0,i-2),i+1};
    }

    private int parseStatusCode(byte[] content,int offset, int length) {
        int finalPosition = offset + length;
        for(int i = 0;i < length;i++) {
            if(content[offset++]!=SPACE) continue;
            else break;
        }

        while(offset<finalPosition) {
            if(content[offset] > 47 && content[offset] < 58) break;
            else offset++;
        }


        if(offset+3<finalPosition) {
            return parseThreeCharacterInt(content,offset);
        } else {
            return -1;
        }
    }

    private int parseThreeCharacterInt(byte[] content,int offset) {
        return ((content[offset] - ZERO) * 100) + ((content[offset+1] - ZERO) * 10) + (content[offset+2] - ZERO);

    }

    private void parseHeader(byte[] content, int lineEnding, int offset,
                             int colonPosition, Map<String,Collection<String>> headers) {
        String key = toString(content, offset, colonPosition - offset);
        String value = toString(content, colonPosition+2, ((lineEnding-3) - colonPosition));
        Collection<String> existing = headers.get(key);
        if(existing==null) {
            existing = new ArrayList<String>(2);
            headers.put(key,existing);
        }
        existing.add(value);
    }

    public CachedResponse parseCachedResponse(byte[] content) {
        Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>();
        int[] statusCodes = parseStatusCode(content);
        int statusCode;
        if(statusCodes[0] == -1) return CachedResponse.MISS;
        else statusCode = statusCodes[0];

        byte prevMinOne = -1;
        byte prevMinTwo = -1;
        byte prev = -1;
        int colon = -1;
        int offset = statusCodes[1];
        for(int i=statusCodes[1];i<content.length;i++) {
            if(content[i] == COLON && colon==-1) {
                colon = i;
                continue;
            }

            if( (content[i] == CacheConfigGlobals.NEW_LINE[1]) && (prev == CacheConfigGlobals.NEW_LINE[0])) {
                if((prevMinTwo == CacheConfigGlobals.NEW_LINE[0]) && (prevMinOne == CacheConfigGlobals.NEW_LINE[1])){
                    offset = i+1;
                    break;
                }
                if (colon != -1) {
                    parseHeader(content,i,offset,colon,headers);
                    colon = -1;
                }
                offset = i+1;
            }

            prevMinTwo = prevMinOne;
            prevMinOne = prev;
            prev = content[i];
        }

        if(headers.size()>0) {
            return new CachedResponse(true,statusCode, headers, content, offset);
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
