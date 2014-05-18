package org.greencheek.web.filter.memcached.hystrix.config;

import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class CacheLookupConfigBuilder {

    public static final boolean DEFAULT_USE_THREAD_POOL = false;
    public static final int DEFAULT_SEMAPHORE_SIZE = 1024;
    public static final int DEFAULT_LOOKUP_TIMEOUT = 200;
    public static final int DEFAULT_BATCHING_TIME = 20;
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_THREAD_POOL_QUEUESIZE = 1024;
    public static final int DEFAULT_BATCHING_MAX_SIZE = 100;

    private boolean useThreadPool = DEFAULT_USE_THREAD_POOL;
    private int semaphoreSize = DEFAULT_SEMAPHORE_SIZE;
    private int lookupTimeout = DEFAULT_LOOKUP_TIMEOUT;
    private int batchingTime = DEFAULT_BATCHING_TIME;
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private int threadPoolQueueSize = DEFAULT_THREAD_POOL_QUEUESIZE;
    private int batchingMaxSize = DEFAULT_BATCHING_MAX_SIZE;


    public CacheLookupConfigBuilder useThreadPool(String executionType) {
        if(executionType == null || executionType.trim().length()==0) return this;

        if(executionType.equalsIgnoreCase("threadpool")) {
            useThreadPool = true;
        } else if(executionType.toLowerCase().contains("thread")) {
            useThreadPool = true;
        }
        else if(executionType.equalsIgnoreCase("semaphore")) {
            useThreadPool = false;
        } else if(executionType.toLowerCase().contains("sema")) {
            useThreadPool = false;
        } else {
            useThreadPool = DEFAULT_USE_THREAD_POOL;
        }
        return this;
    }


    public CacheLookupConfigBuilder setSemaphoreSize(String size) {
        semaphoreSize = CacheConfigGlobals.parseIntValue(size,DEFAULT_SEMAPHORE_SIZE);
        return this;
    }

    public CacheLookupConfigBuilder setBatchingMaxSize(String size) {
        batchingMaxSize = CacheConfigGlobals.parseIntValue(size,DEFAULT_BATCHING_MAX_SIZE);
        return this;
    }

    public CacheLookupConfigBuilder setLookupTimeout(String timeout) {
        lookupTimeout = CacheConfigGlobals.parseIntValue(timeout,DEFAULT_LOOKUP_TIMEOUT);
        return this;
    }

    public CacheLookupConfigBuilder setBatchingTime(String timeout) {
        batchingTime = CacheConfigGlobals.parseIntValue(timeout,DEFAULT_BATCHING_TIME);
        return this;
    }

    public CacheLookupConfigBuilder setThreadPoolSize(String size) {
        threadPoolSize = CacheConfigGlobals.parseIntValue(size,DEFAULT_THREAD_POOL_SIZE);
        return this;
    }

    public CacheLookupConfigBuilder setThreadPoolQueueSize(String size) {
        threadPoolQueueSize = CacheConfigGlobals.parseIntValue(size,DEFAULT_THREAD_POOL_QUEUESIZE);
        return this;
    }

    public CacheLookupConfig build() {
        return new CacheLookupConfig(useThreadPool,semaphoreSize ,lookupTimeout,
                batchingTime,batchingMaxSize,threadPoolSize,threadPoolQueueSize);
    }
}
