package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.client.FilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.MemcachedStorageConfig;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyFilterMemcachedStorage implements FilterMemcachedStorage {


    private final MemcachedClient client;
    private final MemcachedStorageConfig storageConfig;


    public SpyFilterMemcachedStorage(MemcachedClient client, MemcachedStorageConfig config) {
        this.client = client;
        this.storageConfig = config;
    }

    @Override
    public void writeToCache(HttpServletRequest theRequest, BufferedResponseWrapper theResponse) {
        ResizeableByteBuffer buffer = theResponse.getBufferedMemcachedContent();
        boolean hasOverflowed = buffer.canWrite();
        buffer.closeForWrites();

        if(hasOverflowed) return;

        String key = storageConfig.getCacheKeyCreator().createCacheKey(theRequest);


        if(storageConfig.isForceCache()) {
            writeToCache(key, storageConfig.getForceCacheDurationInSeconds(), storageConfig.getCustomHeaders(),
                         getHeaders(storageConfig.getResponseHeadersToIgnore(),theResponse),buffer);
        }
        else {
            String cacheControlHeader = theResponse.getHeader(CacheConfigGlobals.CACHE_CONTROL_HEADER);
            boolean canCache = cacheControlHeader!=null && storageConfig.getPatternForNoCacheMatching().matcher(cacheControlHeader).find();
            if(!canCache) {
                return;
            }
            else {
                writeToCache(key, storageConfig.getForceCacheDurationInSeconds(), storageConfig.getCustomHeaders(),
                             getHeaders(storageConfig.getResponseHeadersToIgnore(),theResponse),buffer);
            }

        }


//        ResizeableByteBuffer bufferedContent = servletResponse.getBufferedMemcachedContent();
//        boolean shouldWriteToMemcached = bufferedContent.canWrite();
//        bufferedContent.closeForWrites();
//        if(bufferedContent!=null && shouldWriteToMemcached) {
//            String key = createKey(servletRequest);
//            filterMemcachedStorage.writeToCache(key, 10, Collections.EMPTY_SET, getHeaders(DEFAULT_HEADERS_TO_IGNORE, servletResponse), servletResponse.getBufferedMemcachedContent());
//        }
    }

    private Map<String,Collection<String>> getHeaders(Set<String> headerNamesToIngore,
                                                      BufferedResponseWrapper servletResponse) {
        Collection<String> headerNames = servletResponse.getHeaderNames();
        Map<String,Collection<String>> headers = new HashMap<String, Collection<String>>(headerNames.size());

        for(String key : headerNames) {
            if(headerNamesToIngore.contains(key.toLowerCase())) continue;
            headers.put(key,servletResponse.getHeaders(key));
        }


        return headers;
    }

    private void writeToCache(String key, int expiryInSeconds, Set<String> additionalContent,
                             Map<String, Collection<String>> responseHeaders, ResizeableByteBuffer content) {
        int contentLength = content.size();
        ResizeableByteBuffer memcachedContent = new ResizeableByteBuffer(contentLength,contentLength + storageConfig.getHeadersLength());

        for(String string : additionalContent) {
            addStringToContent(memcachedContent,string);
            memcachedContent.append(CacheConfigGlobals.NEW_LINE);
        }


        for(Map.Entry<String,Collection<String>> entry : responseHeaders.entrySet()) {
            byte[] headerName = getBytes(entry.getKey());
            for(String value : entry.getValue()) {
                memcachedContent.append(headerName);
                memcachedContent.append(CacheConfigGlobals.HEADER_NAME_SEPARATOR);
                addStringToContent(memcachedContent, value);
                memcachedContent.append(CacheConfigGlobals.NEW_LINE);
            }
        }



        memcachedContent.append(CacheConfigGlobals.NEW_LINE);
        memcachedContent.append(CacheConfigGlobals.NEW_LINE);
        memcachedContent.append(content.getBuf(),0,contentLength);

        writeToMemcached(key, expiryInSeconds, memcachedContent.trim().getBuf());

    }

    private void addStringToContent(ResizeableByteBuffer content, String value) {
        content.append(getBytes(value));
    }

    private byte[] getBytes(String content) {
        try {
            return content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return content.getBytes();
        }
    }

    private void writeToMemcached(String key, int expiryInSeconds, byte[] content) {
        try {
            client.set(key, expiryInSeconds, content);
        } catch (Exception e) {
            // need a logger
        }
    }
}
