package org.greencheek.web.filter.memcached.client.config;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedFetchingConfig {

    private final long cacheGetTimeoutInMillis;

    public MemcachedFetchingConfig(long cacheGetTimeout) {
        this.cacheGetTimeoutInMillis = cacheGetTimeout;
    }


    public long getCacheGetTimeoutInMillis() {
        return cacheGetTimeoutInMillis;
    }

}
