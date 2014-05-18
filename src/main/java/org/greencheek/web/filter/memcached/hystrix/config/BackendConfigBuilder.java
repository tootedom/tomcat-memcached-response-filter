package org.greencheek.web.filter.memcached.hystrix.config;

import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class BackendConfigBuilder {

    public static final int DEFAULT_SEMAPHORE_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_BACKEND_TIMEOUT = 60000;
    public static final boolean DEFAULT_BACKEND_HYSTRIX_ENABLED = true;

    private int semaphoreSize = DEFAULT_SEMAPHORE_SIZE;
    private int backendTimeout = DEFAULT_BACKEND_TIMEOUT;
    private boolean isHystrixEnabled = DEFAULT_BACKEND_HYSTRIX_ENABLED;


    public BackendConfigBuilder setSemaphoreSize(String size) {
        semaphoreSize = CacheConfigGlobals.parseIntValue(size,DEFAULT_SEMAPHORE_SIZE);
        return this;
    }

    public BackendConfigBuilder setBackendTimeout(String timeout) {
        backendTimeout = CacheConfigGlobals.parseIntValue(timeout,DEFAULT_BACKEND_TIMEOUT);
        return this;
    }

    public BackendConfigBuilder setBackendHystrixEnabled(boolean enabled) {
        isHystrixEnabled = enabled;
        return this;
    }


    public BackendConfig build() {
        return new BackendConfig(semaphoreSize ,backendTimeout,isHystrixEnabled);
    }
}
