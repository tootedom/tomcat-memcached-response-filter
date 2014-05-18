package org.greencheek.web.filter.memcached;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.hystrix.commands.CacheLookupCommand;
import org.greencheek.web.filter.memcached.hystrix.config.CacheLookupConfig;
import org.greencheek.web.filter.memcached.hystrix.config.CacheLookupConfigBuilder;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Uses Hystrix  Commands (https://github.com/Netflix/Hystrix), to monitor, and batch, request to memcached
 * Created by dominictootell on 17/05/2014.
 */
public class HystrixPublishToMemcachedFilter extends PublishToMemcachedFilter {

    public static final AtomicInteger filterReferenceCount = new AtomicInteger(0);

    public static final String MEMCACHED_HYSTRIX_CACHE_LOOKUP_EXECUTION_TYPE = "memcached-hystrix-cachelookup-exec-type";
    public static final String MEMCACHED_HYSTRIX_CACHE_LOOKUP_SEMAPHORE_SIZE = "memcached-hystrix-cachelookup-semaphore-size";
    public static final String MEMCACHED_HYSTRIX_CACHE_LOOKUP_TIMEOUT_MILLIS = "memcached-hystrix-cachelookup-timeout-millis";
    public static final String MEMCACHED_HYSTRIX_CACHE_LOOKUP_BATCHING_TIME_MILLIS = "memcached-hystrix-cachelookup-batchingtime-millis";
    public static final String MEMCACHED_HYSTRIX_CACHE_LOOKUP_BATCHING_MAX_SIZE = "memcached-hystrix-cachelookup-batching-maxsize";
    public static final String MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_SIZE = "memcached-hystrix-cachelookup-threadpool-size";
    public static final String MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_QUEUESIZE = "memcached-hystrix-cachelookup-threadpool-queuesize";


    private CacheLookupConfig cacheLookupConfig;
    private HystrixCommand.Setter cacheLookupSettings;
    private HystrixCollapser.Setter batchLookupSettings;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        filterReferenceCount.incrementAndGet();

        CacheLookupConfigBuilder lookupConfigBuilder = new CacheLookupConfigBuilder();
        lookupConfigBuilder.setBatchingTime(filterConfig.getInitParameter(MEMCACHED_HYSTRIX_CACHE_LOOKUP_BATCHING_TIME_MILLIS));
        lookupConfigBuilder.setBatchingMaxSize(filterConfig.getInitParameter(MEMCACHED_HYSTRIX_CACHE_LOOKUP_BATCHING_MAX_SIZE));
        lookupConfigBuilder.useThreadPool(filterConfig.getInitParameter(MEMCACHED_HYSTRIX_CACHE_LOOKUP_EXECUTION_TYPE));
        lookupConfigBuilder.setSemaphoreSize(filterConfig.getInitParameter(MEMCACHED_HYSTRIX_CACHE_LOOKUP_SEMAPHORE_SIZE));
        lookupConfigBuilder.setLookupTimeout(filterConfig.getInitParameter(MEMCACHED_HYSTRIX_CACHE_LOOKUP_TIMEOUT_MILLIS));
        lookupConfigBuilder.setThreadPoolSize(filterConfig.getInitParameter(MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_SIZE));
        lookupConfigBuilder.setThreadPoolQueueSize(filterConfig.getInitParameter(MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_QUEUESIZE));

        cacheLookupConfig = lookupConfigBuilder.build();

        batchLookupSettings = CacheLookupCommand.createCollapserSettings(cacheLookupConfig);
        cacheLookupSettings = CacheLookupCommand.createCacheLookupCommandSettings(cacheLookupConfig);

        super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            super.doFilter(request,response,chain);
        }  finally {
            context.shutdown();
        }
    }

        /**
         * Execute cache lookup
         * @param cacheKey
         */
    @Override
    public CachedResponse executeCacheLookup(String cacheKey) {
        return new CacheLookupCommand(cacheKey,getMemcachedFetchingImpl(),batchLookupSettings,cacheLookupSettings).execute();
    }

    /**
     * Post the backend request's completion
     * @param servletRequest
     * @param theResponse
     */
    @Override
    public void postFilter(HttpServletRequest servletRequest,BufferedResponseWrapper theResponse) {
        storeResponseInMemcached(servletRequest, theResponse);
    }

    @Override
    public void destroy() {
        super.destroy();
        shutdown();
    }

    public void shutdown() {
        if(filterReferenceCount.decrementAndGet()==0) {
            com.netflix.hystrix.Hystrix.reset();
        }
    }
}
