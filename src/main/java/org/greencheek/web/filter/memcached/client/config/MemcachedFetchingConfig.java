package org.greencheek.web.filter.memcached.client.config;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedFetchingConfig {

    private final long cacheGetTimeoutInMillis;
    private final String[] noCacheHeaders;

    public MemcachedFetchingConfig(long cacheGetTimeout, String[] noCacheHeaders) {
        this.cacheGetTimeoutInMillis = cacheGetTimeout;
        this.noCacheHeaders = noCacheHeaders;
    }


    public long getCacheGetTimeoutInMillis() {
        return cacheGetTimeoutInMillis;
    }

    public String[] getNoCacheHeaders() {
        return noCacheHeaders;
    }
}
