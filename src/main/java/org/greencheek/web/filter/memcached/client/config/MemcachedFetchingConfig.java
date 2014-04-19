package org.greencheek.web.filter.memcached.client.config;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedFetchingConfig {

    private final long cacheGetTimeoutInMillis;
    private final MemcachedKeyConfig keyConfig;
    private final String[] noCacheHeaders;

    public MemcachedFetchingConfig(MemcachedKeyConfig keyConfig,
                                   long cacheGetTimeout, String[] noCacheHeaders) {
        this.keyConfig = keyConfig;
        this.cacheGetTimeoutInMillis = cacheGetTimeout;
        this.noCacheHeaders = noCacheHeaders;
    }

    public MemcachedKeyConfig getKeyConfig() {
        return keyConfig;
    }

    public long getCacheGetTimeoutInMillis() {
        return cacheGetTimeoutInMillis;
    }

    public String[] getNoCacheHeaders() {
        return noCacheHeaders;
    }
}
