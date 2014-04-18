package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.client.config.Duration;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfigBuilder {
    public static int DEFAULT_MAX_HEADERS_LENGTH_TO_STORE = 8192;
    public static Set<String> DEFAULT_ADDITIONAL_HEADERS = Collections.EMPTY_SET;
    public static final int DEFAULT_EXPIRY_IN_SECONDS = 300;
//    public static final String DEFAULT_CACHE_KEY = "$scheme$request_method$request_uri$header_accept$header_accept-encoding";
    public static final boolean DEFAULT_STORE_PRIVATE = false;
    public static final boolean DEFAULT_FORCE_CACHE = false;
    public static final int DEFAULT_FORCE_CACHE_DURATION = DEFAULT_EXPIRY_IN_SECONDS;
    public static final byte[] DEFAULT_HTTP_STATUS_LINE = new byte[]{'H','T','T','P','/','1','.','1',' '};
    public static final MaxAgeParser DEFAULT_MAX_AGE_PARSER = new DefaultMaxAgeParser();

//    public static final KeyHashing DEFAULT_MESSAGE_HASHING = new MessageDigestHashing();

//    public static final MemcachedKeyConfig DEFAULT_KEY_CONFIG = new MemcachedKeyConfig(new DefaultCacheKeyCreator(DEFAULT_CACHE_KEY),DEFAULT_MESSAGE_HASHING);

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
        headers.add("date");
        DEFAULT_RESPONSE_HEADERS_TO_IGNORE = Collections.unmodifiableSet(headers);
    }


    private Set<String> responseHeadersToIgnore = DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    private int defaultExpiryInSeconds = DEFAULT_EXPIRY_IN_SECONDS;
    private Set<String> additionalHeaders  = DEFAULT_ADDITIONAL_HEADERS;
    private int defaultMaxHeadersLengthToStore = DEFAULT_MAX_HEADERS_LENGTH_TO_STORE;
    private boolean storePrivate = DEFAULT_STORE_PRIVATE;
    private boolean forceCache = DEFAULT_FORCE_CACHE;
    private int forceCacheDuration = DEFAULT_FORCE_CACHE_DURATION;
    private byte[] httpStatusLinePrefix = DEFAULT_HTTP_STATUS_LINE;
    private MaxAgeParser maxAgeParser = DEFAULT_MAX_AGE_PARSER;
    private boolean canCacheWithNoCacheControl = true;

    private MemcachedKeyConfig keyConfig;


    public MemcachedStorageConfigBuilder(MemcachedKeyConfig keyConfig) {
        this.keyConfig = keyConfig;
    }

    public MemcachedStorageConfig build() {
        return new MemcachedStorageConfig(defaultMaxHeadersLengthToStore,keyConfig,defaultExpiryInSeconds,
                additionalHeaders,responseHeadersToIgnore,storePrivate,forceCache,
                forceCacheDuration,httpStatusLinePrefix,maxAgeParser,canCacheWithNoCacheControl);
    }

    public MemcachedStorageConfigBuilder setKeyConfig(MemcachedKeyConfig keyConfig) {
        this.keyConfig = keyConfig;
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

    public MemcachedStorageConfigBuilder setAdditionalCustomHeaders(Set<String> customHeaders) {
        this.additionalHeaders = new HashSet<String>(customHeaders);
        return this;
    }

    public MemcachedStorageConfigBuilder setAdditionalCustomHeaders(String[] customHeaders) {
        this.additionalHeaders = new HashSet<String>(customHeaders.length);
        for(String customHeader : customHeaders) {
            additionalHeaders.add(customHeader);
        }
        return this;
    }

    public MemcachedStorageConfigBuilder setStorePrivate(boolean storePrivate) {
        this.storePrivate = storePrivate;
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCache(boolean forceCache) {
        this.forceCache = forceCache;
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCacheDuration(Duration duration) {
        this.forceCacheDuration = (int)duration.toSeconds();
        return this;
    }

    public MemcachedStorageConfigBuilder setHttpStatusLinePrefix(String prefix) {
        prefix = prefix.trim();
        this.httpStatusLinePrefix = CacheConfigGlobals.getBytes(prefix);
        return this;
    }


    public MemcachedStorageConfigBuilder setMaxAgeParser(MaxAgeParser maxAgeParser) {
        this.maxAgeParser = maxAgeParser;
        return this;
    }

    public MemcachedStorageConfigBuilder setCanCacheWithNoCacheControl(boolean canCacheWithNoCacheControl) {
        this.canCacheWithNoCacheControl = canCacheWithNoCacheControl;
        return this;
    }
}
