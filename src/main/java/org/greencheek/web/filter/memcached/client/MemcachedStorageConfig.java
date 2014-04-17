package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfig {

    public static final Pattern NO_STORE_CACHE_PRIVATE_CACHE_RESPONSE_HEADER = Pattern.compile("no-store|no-cache");
    public static final Pattern NO_STORE_NO_PRIVATE_CACHE_RESPONSE_HEADER = Pattern.compile("no-store|no-cache|private");
    public static final String REQUEST_NO_STORE_REQUEST_VALUE = "no-cache";

    private final int headersLength;
    private final CacheKeyCreator cacheKeyCreator;
    private final int defaultExpiryInSeconds;
    private final Set<String> responseHeadersToIgnore;
    private final Set<String> customHeaders;
    private final boolean storePrivate;
    private final Pattern patternForNoCacheMatching;
    private final boolean forceCache;
    private final int forceCacheDurationInSeconds;
    private final String httpStatusLinePrefix;


    public MemcachedStorageConfig(int headersLength, CacheKeyCreator cacheKeyCreator,
                                  int defaultExpiryInSeconds, Set<String> customHeaders, Set<String> responseHeadersToIgnore,
                                  boolean storePrivate,boolean forceCache,int forceCacheDurationInSeconds,
                                  String httpStatusLinePrefix) {
        this.headersLength = headersLength;
        this.cacheKeyCreator = cacheKeyCreator;
        this.defaultExpiryInSeconds = defaultExpiryInSeconds;
        this.customHeaders = customHeaders;
        this.responseHeadersToIgnore = responseHeadersToIgnore;
        this.storePrivate = storePrivate;

        if(storePrivate) {
            patternForNoCacheMatching = NO_STORE_CACHE_PRIVATE_CACHE_RESPONSE_HEADER;
        } else {
            patternForNoCacheMatching = NO_STORE_NO_PRIVATE_CACHE_RESPONSE_HEADER;
        }

        this.forceCache = forceCache;
        this.forceCacheDurationInSeconds = forceCacheDurationInSeconds;
        this.httpStatusLinePrefix = httpStatusLinePrefix;
    }


    public int getHeadersLength() {
        return headersLength;
    }

    public CacheKeyCreator getCacheKeyCreator() {
        return cacheKeyCreator;
    }

    public int getDefaultExpiryInSeconds() {
        return defaultExpiryInSeconds;
    }

    public Set<String> getResponseHeadersToIgnore() {
        return responseHeadersToIgnore;
    }

    public Set<String> getCustomHeaders() {
        return customHeaders;
    }

    public boolean isStorePrivate() {
        return storePrivate;
    }

    public Pattern getPatternForNoCacheMatching() {
        return patternForNoCacheMatching;
    }

    public boolean isForceCache() {
        return forceCache;
    }

    public int getForceCacheDurationInSeconds() {
        return forceCacheDurationInSeconds;
    }

    public String getHttpStatusLinePrefix() {
        return httpStatusLinePrefix;
    }
}
