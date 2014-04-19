package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.client.cachecontrol.CacheControlResponseDecider;
import org.greencheek.web.filter.memcached.client.cachecontrol.MaxAgeParser;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfig {

    private final int headersLength;
    private final MemcachedKeyConfig cacheKeyCreator;
    private final int defaultExpiryInSeconds;
    private final Set<String> responseHeadersToIgnore;
    private final Set<String> customHeaders;
    private final CacheControlResponseDecider cacheResponseDecider;
    private final boolean forceCache;
    private final int forceCacheDurationInSeconds;
    private final byte[] httpStatusLinePrefix;
    private final MaxAgeParser maxAgeParser;
    private final boolean canCacheWithNoCacheControlHeader;



    public MemcachedStorageConfig(int headersLength, MemcachedKeyConfig cacheKeyCreator,
                                  int defaultExpiryInSeconds, Set<String> customHeaders, Set<String> responseHeadersToIgnore,
                                  CacheControlResponseDecider cacheResponseDecider,boolean forceCache,int forceCacheDurationInSeconds,
                                  byte[] httpStatusLinePrefix,MaxAgeParser maxAgeParser,boolean canCacheWithNoCacheControlHeader
                                  ) {
        this.headersLength = headersLength;
        this.cacheKeyCreator = cacheKeyCreator;
        this.defaultExpiryInSeconds = defaultExpiryInSeconds;
        this.customHeaders = customHeaders;
        this.responseHeadersToIgnore = responseHeadersToIgnore;
        this.cacheResponseDecider = cacheResponseDecider;
        this.forceCache = forceCache;
        this.forceCacheDurationInSeconds = forceCacheDurationInSeconds;
        this.httpStatusLinePrefix = httpStatusLinePrefix;
        this.maxAgeParser = maxAgeParser;
        this.canCacheWithNoCacheControlHeader = canCacheWithNoCacheControlHeader;
    }


    public int getHeadersLength() {
        return headersLength;
    }

    public MemcachedKeyConfig getCacheKeyCreator() {
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

    public CacheControlResponseDecider getCacheResponseDecider() {
        return cacheResponseDecider;
    }

    public boolean isForceCache() {
        return forceCache;
    }

    public int getForceCacheDurationInSeconds() {
        return forceCacheDurationInSeconds;
    }

    public byte[] getHttpStatusLinePrefix() {
        return httpStatusLinePrefix;
    }


    public MaxAgeParser getMaxAgeParser() {
        return maxAgeParser;
    }

    public boolean isCanCacheWithNoCacheControlHeader() {
        return canCacheWithNoCacheControlHeader;
    }
}
