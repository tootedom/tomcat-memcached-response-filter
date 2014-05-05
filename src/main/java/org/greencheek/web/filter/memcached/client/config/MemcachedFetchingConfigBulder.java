package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.domain.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedFetchingConfigBulder {

    public static final long DEFAULT_CACHE_GET_TIMEOUT_IN_MILLIS = 1000;

    private long cacheGetTimeout = DEFAULT_CACHE_GET_TIMEOUT_IN_MILLIS;


    private String[] noCacheClientValues = CacheConfigGlobals.NO_CACHE_CLIENT_VALUE;


    public MemcachedFetchingConfigBulder() {

    }

    public MemcachedFetchingConfig build() {
        return new MemcachedFetchingConfig(cacheGetTimeout,noCacheClientValues);
    }



    public MemcachedFetchingConfigBulder setCacheGetTimeout(Duration cacheGetTimeout) {
        this.cacheGetTimeout = cacheGetTimeout.toMillis();
        return this;
    }

    public MemcachedFetchingConfigBulder setCacheGetTimeout(String cacheGetTimeout) {
        if (cacheGetTimeout == null) return this;
        cacheGetTimeout = cacheGetTimeout.trim();
        if (cacheGetTimeout.length() > 0) {
            try {
                this.cacheGetTimeout = new Duration(Integer.parseInt(cacheGetTimeout), TimeUnit.MILLISECONDS).toMillis();
            } catch (NumberFormatException e) {

            }
        }
        return this;
    }

    public MemcachedFetchingConfigBulder noCacheClientValues(String values) {
        if(values == null || values.trim().length() == 0) {
            noCacheClientValues = new String[0];
        } else {
            noCacheClientValues = values.split(",");
        }
        return this;
    }
}
