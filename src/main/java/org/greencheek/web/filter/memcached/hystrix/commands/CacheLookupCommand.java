package org.greencheek.web.filter.memcached.hystrix.commands;

import com.netflix.hystrix.*;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.hystrix.config.CacheLookupConfig;

import java.util.*;

/**
 * Created by dominictootell on 17/05/2014.
 */
public class CacheLookupCommand extends HystrixCollapser<Map<String,CachedResponse>, CachedResponse, String> {

    private final String cacheKey;
    private final FilterMemcachedFetching cacheLookupImpl;
    private final HystrixCommand.Setter commandSettings;

    public CacheLookupCommand(String cacheKey,FilterMemcachedFetching memcachedFetching,
                              HystrixCollapser.Setter batchSettings, HystrixCommand.Setter commandSettings) {
        super(batchSettings);
        this.commandSettings = commandSettings;
        cacheLookupImpl = memcachedFetching;
        this.cacheKey = cacheKey;
    }

    public static HystrixCollapser.Setter createCollapserSettings(CacheLookupConfig cacheLookupConfig) {
        return Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("CacheLookupCollapser"))
                .andScope(Scope.GLOBAL)
                .andCollapserPropertiesDefaults(
                        HystrixCollapserProperties.Setter()
                                .withMaxRequestsInBatch(cacheLookupConfig.getBatchingMaxSize())
                                .withTimerDelayInMilliseconds(cacheLookupConfig.getBatchingTimeInMillis())
                );
    }

    public static HystrixCommand.Setter createCacheLookupCommandSettings(CacheLookupConfig cacheLookupConfig) {
        HystrixCommand.Setter s = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CacheLookupGroup"));
        if(cacheLookupConfig.isUseThreadPool()){
            s.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(cacheLookupConfig.getThreadPoolSize())
                    .withMaxQueueSize(cacheLookupConfig.getThreadPoolQueueSize()));
            s.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD));
            s.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("CacheLookup"));
        } else {
            s.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                            .withExecutionIsolationSemaphoreMaxConcurrentRequests(cacheLookupConfig.getSemaphoreSize())
            );
        }
        s.andCommandKey(HystrixCommandKey.Factory.asKey("CacheLookup"));
        return s;
    }

    @Override
    public String getRequestArgument() {
        return cacheKey;
    }

    @Override
    protected HystrixCommand<Map<String, CachedResponse>> createCommand(Collection<CollapsedRequest<CachedResponse, String>> collapsedRequests) {
        return new BatchCommand(commandSettings,cacheLookupImpl,collapsedRequests);
    }

    @Override
    protected void mapResponseToRequests(Map<String, CachedResponse> batchResponse, Collection<CollapsedRequest<CachedResponse, String>> collapsedRequests) {
        for (CollapsedRequest<CachedResponse, String> request : collapsedRequests) {
            request.setResponse(batchResponse.get(request.getArgument()));
        }
    }

    @Override
    protected String getCacheKey() {
        return cacheKey;
    }


    private static final class BatchCommand extends HystrixCommand<Map<String,CachedResponse>> {
        private final Collection<CollapsedRequest<CachedResponse, String>>  requests;
        private final FilterMemcachedFetching cacheLookupImpl;


        private BatchCommand(HystrixCommand.Setter commandSettings,FilterMemcachedFetching memcachedFetcher,
                             Collection<CollapsedRequest<CachedResponse, String>> requests) {

            super(commandSettings);
            this.requests = requests;
            this.cacheLookupImpl = memcachedFetcher;
        }

        @Override
        protected Map<String,CachedResponse> run() {
            Set<String> deDupedRequests = new HashSet<String>(requests.size(),1.0f);
            for (CollapsedRequest<CachedResponse, String> request : requests) {
                deDupedRequests.add(request.getArgument());
            }
            return cacheLookupImpl.getCachedContent(deDupedRequests);
        }

        @Override
        protected Map<String,CachedResponse> getFallback() {
            Map<String,CachedResponse> responses = new HashMap<String,CachedResponse>(requests.size(),1.0f);
            for(CollapsedRequest<CachedResponse,String> request : requests) {
                responses.put(request.getArgument(), CachedResponse.MISS);
            }
            return responses;
        }
//
    }
}