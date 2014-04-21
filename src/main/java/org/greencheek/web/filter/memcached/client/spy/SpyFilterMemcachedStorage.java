package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import org.greencheek.web.filter.memcached.cachekey.CacheKey;
import org.greencheek.web.filter.memcached.client.FilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.config.MemcachedStorageConfig;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyFilterMemcachedStorage implements FilterMemcachedStorage {

    private static final Logger logger = LoggerFactory.getLogger(SpyFilterMemcachedStorage.class);

    private final MemcachedClient client;
    private final MemcachedStorageConfig storageConfig;


    public SpyFilterMemcachedStorage(MemcachedClient client, MemcachedStorageConfig config) {
        this.client = client;
        this.storageConfig = config;
    }

    @Override
    public void writeToCache(HttpServletRequest theRequest, BufferedResponseWrapper theResponse) {
        ResizeableByteBuffer buffer = theResponse.getBufferedMemcachedContent();
        if(buffer==null) return;
        boolean hasOverflowed = !buffer.canWrite();
        buffer.closeForWrites();

        if(hasOverflowed) return;

        if(storageConfig.isForceCache()) {
            writeToCache(theRequest,theResponse, storageConfig.getForceCacheDurationInSeconds(), storageConfig.getCustomHeaders(),
                         getHeaders(storageConfig.getResponseHeadersToIgnore(),theResponse),buffer);
        }
        else {
            String cacheControlHeader = theResponse.getHeader(CacheConfigGlobals.CACHE_CONTROL_HEADER);
            if (cacheControlHeader == null) {
                if (!storageConfig.isCanCacheWithNoCacheControlHeader()) {
                    return;
                } else {
                    writeToCache(theRequest, theResponse,
                            storageConfig.getDefaultExpiryInSeconds(),
                            storageConfig.getCustomHeaders(), getHeaders(storageConfig.getResponseHeadersToIgnore(), theResponse),
                            buffer);
                }
            } else {
                boolean canCache = storageConfig.getCacheResponseDecider().isCacheable(cacheControlHeader);
                if (!canCache) {
                    return;
                } else {
                    writeToCache(theRequest, theResponse,
                            storageConfig.getMaxAgeParser().maxAge(cacheControlHeader, storageConfig.getDefaultExpiryInSeconds()),
                            storageConfig.getCustomHeaders(), getHeaders(storageConfig.getResponseHeadersToIgnore(), theResponse),
                            buffer);
                }
            }
        }

    }

    /**
     * Adds a "Content-Length" header to the buffer
     *
     * @param theResponse
     * @param buffer
     */
    private void addContentLengthHeader(BufferedResponseWrapper theResponse,ResizeableByteBuffer buffer) {
        int length = theResponse.getContentLength();
        if(length < 1) {
            length = theResponse.getBufferedMemcachedContent().size();
        }
        buffer.append(CacheConfigGlobals.CONTENT_LENGTH_HEADER_AS_BYTES);
        buffer.append(CacheConfigGlobals.HEADER_NAME_SEPARATOR);
        buffer.append(CacheConfigGlobals.toByteArray(length));
        buffer.append(CacheConfigGlobals.NEW_LINE);
    }

    /**
     * Add HTTP/1.1 200 OK
     * @param theResponse
     * @param buffer
     */
    private void addHttpStatusLine(BufferedResponseWrapper theResponse, ResizeableByteBuffer buffer) {
        int status = theResponse.getStatus();
        if(status<100) {
            buffer.closeForWrites();
            return;
        }

        buffer.append(storageConfig.getHttpStatusLinePrefix());
        buffer.append(CacheConfigGlobals.getStatusCodeText(status));
        buffer.append(CacheConfigGlobals.NEW_LINE);
    }

    private void addContentTypeHeader(BufferedResponseWrapper theResponse,ResizeableByteBuffer buffer) {
        String contentType = theResponse.getContentType();
        buffer.append(CacheConfigGlobals.CONTENT_TYPE_HEADER_AS_BYTES);
        buffer.append(CacheConfigGlobals.HEADER_NAME_SEPARATOR);

        if(contentType == null || contentType.length()==0) {
            buffer.append(CacheConfigGlobals.DEFAULT_CONTENT_TYPE_HEADER_VALUE_AS_BYTES);
            buffer.append(CacheConfigGlobals.NEW_LINE);
        } else {
            buffer.append(getBytes(contentType));
            buffer.append(CacheConfigGlobals.NEW_LINE);
        }
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


    private void writeToCache(HttpServletRequest theRequest,BufferedResponseWrapper theResponse,
                              int expiryInSeconds, Set<String> additionalContent,
                              Map<String,Collection<String>> responseHeaders, ResizeableByteBuffer content) {
        if(!storageConfig.canStatusCodeBeCached(theResponse.getStatus())) {
            return;
        }

        int contentLength = content.size();
        ResizeableByteBuffer memcachedContent = new ResizeableByteBuffer(contentLength,contentLength + storageConfig.getHeadersLength());

        addHttpStatusLine(theResponse,memcachedContent);
        addContentLengthHeader(theResponse,memcachedContent);
        addContentTypeHeader(theResponse,memcachedContent);
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
        memcachedContent.append(content.getBuf(),0,contentLength);


        if(memcachedContent.canWrite()) {
            CacheKey key = storageConfig.getCacheKeyCreator().createCacheKey(theRequest);
            if(key.isFullyPopulated()) {
                writeToMemcached(key.getKey(), expiryInSeconds, memcachedContent.trim().getBuf());
            }
        }

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
            logger.warn("Unable to write to memcached",e);
        }
    }
}
