package org.greencheek.web.filter.memcached.hystrix.commands;

import com.netflix.hystrix.*;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.hystrix.config.BackendConfig;
import org.greencheek.web.filter.memcached.hystrix.config.CacheLookupConfig;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by dominictootell on 17/05/2014.
 */
public class BackEndCommand extends HystrixCommand<Void> {

    private final FilterChain filterChain;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public BackEndCommand(Setter settings,
                          FilterChain chain,HttpServletRequest request,
                          HttpServletResponse response) {
        super(settings);

        this.filterChain = chain;
        this.request = request;
        this.response = response;
    }

    @Override
    protected Void run() throws Exception {
        filterChain.doFilter(request, response);
        return null;
    }

    public static Setter createBackendSettring(BackendConfig backendConfig) {
        Setter s = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("BackEndGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerEnabled(false)
                        .withRequestCacheEnabled(false)
                        .withRequestLogEnabled(false)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(backendConfig.getSemaphoreSize())
                );
        return s;
    }
}