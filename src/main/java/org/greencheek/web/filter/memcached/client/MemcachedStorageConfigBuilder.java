package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator;
import org.greencheek.web.filter.memcached.client.config.Duration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfigBuilder {
    public static int DEFAULT_MAX_HEADERS_LENGTH_TO_STORE = 8192;
    public static Set<String> DEFAULT_ADDITIONAL_HEADERS = Collections.EMPTY_SET;
    public final int DEFAULT_EXPIRY_IN_SECONDS = 300;
    public final String DEFAULT_CACHE_KEY = "$scheme$request_method$request_uri$header_accept$header_accept-encoding";

    private static final Set<String> DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    static {
        Set<String> headers = new HashSet<String>(9);
        headers.add("connection");
        headers.add("keep-alive");
        headers.add("proxy-authenticate");
        headers.add("proxy-authorization");
        headers.add("te");
        headers.add("trailers");
        headers.add("transfer-encoding");
        headers.add("upgrade");
        headers.add("set-cookie");
        DEFAULT_RESPONSE_HEADERS_TO_IGNORE = Collections.unmodifiableSet(headers);
    }


    private Set<String> responseHeadersToIgnore = DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    private int defaultExpiryInSeconds = DEFAULT_EXPIRY_IN_SECONDS;
    private Set<String> additionalHeaders  = DEFAULT_ADDITIONAL_HEADERS;
    private int defaultMaxHeadersLengthToStore = DEFAULT_MAX_HEADERS_LENGTH_TO_STORE;
    private CacheKeyCreator cacheKeyCreator = new DefaultCacheKeyCreator(DEFAULT_CACHE_KEY);



    public MemcachedStorageConfig build() {
        return new MemcachedStorageConfig()
    }

    public MemcachedStorageConfigBuilder setCacheKey(String cacheKey) {
        this.cacheKeyCreator = new DefaultCacheKeyCreator(cacheKey);
        return this;
    }

    public MemcachedStorageConfigBuilder setDefaultExpiry(Duration duration) {
        defaultExpiryInSeconds = (int)duration.toSeconds();
        return this;
    }

    public MemcachedStorageConfigBuilder setMaxHeadersSize(int lengthInBytes) {
        this.defaultMaxHeadersLengthToStore = lengthInBytes;
        return this;
    }

    public MemcachedStorageConfigBuilder setResponseHeaderToIgnore(Set<String> headersToIgnore) {
        this.responseHeadersToIgnore = new HashSet<String>(headersToIgnore);
        return this;
    }

    public MemcachedStorageConfigBuilder setResponseHeaderToIgnore(String[] headersToIgnore) {
        this.responseHeadersToIgnore = new HashSet<String>(headersToIgnore.length);
        for(String header : headersToIgnore) {
            responseHeadersToIgnore.add(header);
        }
        return this;
    }



}
