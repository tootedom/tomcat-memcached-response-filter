package org.greencheek.web.filter.memcached.client;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedFetchingConfig {

    private final long cacheGetTimeoutInMillis;
    private final MemcachedKeyConfig keyConfig;

    public MemcachedFetchingConfig(MemcachedKeyConfig keyConfig,
                                   long cacheGetTimeout) {
        this.keyConfig = keyConfig;
        this.cacheGetTimeoutInMillis = cacheGetTimeout;
    }

    public MemcachedKeyConfig getKeyConfig() {
        return keyConfig;
    }

    public long getCacheGetTimeoutInMillis() {
        return cacheGetTimeoutInMillis;
    }
}
