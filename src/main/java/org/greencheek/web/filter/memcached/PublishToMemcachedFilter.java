/*
Copyright 2012 Dominic Tootell

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.greencheek.web.filter.memcached;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.greencheek.web.filter.memcached.client.*;
import org.greencheek.web.filter.memcached.client.cachecontrol.writeable.CacheControlWriteToCacheDecider;
import org.greencheek.web.filter.memcached.client.cachecontrol.writeable.WriteToCacheDecider;
import org.greencheek.web.filter.memcached.client.config.*;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedBuilder;
import org.greencheek.web.filter.memcached.dateformatting.DateHeaderFormatter;
import org.greencheek.web.filter.memcached.dateformatting.QueueBasedDateFormatter;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.response.BufferedRequestWrapper;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;
import org.greencheek.web.filter.memcached.response.Servlet2BufferedResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PublishToMemcachedFilter implements Filter {
    public final static int DEFAULT_MAX_CACHEABLE_RESPONSE_BODY = 8192*2;

    public final static String MEMCACHED_HOSTS_PARAM = "memcached-hosts";
    public final static String MEMCACHED_KEY_PARAM = "memcached-key";
    public final static String MEMCACHED_HEADERS_TO_IGNORE = "memcached-ignore-headers";
    public final static String MEMCACHED_RESPONSE_BODY_SIZE = "memcached-maxcacheable-bodysize";
    public final static String MEMCACHED_HEADER_SIZE = "memcached-header-size";
    public final static String MEMCACHED_GET_TIMEOUT = "memcached-get-timeout-seconds";
    public final static String MEMCACHED_CACHE_PRIVATE = "memcached-cache-private";
    public final static String MEMCACHED_FORCE_CACHE = "memcached-force-cache";
    public final static String MEMCACHED_EXPIRY = "memcached-expiry";
    public final static String MEMCACHED_FORCE_EXPIRY = "memcached-forced-expiry";
    public final static String MEMCACHED_CACHE_STATUS_HEADER_NAME = "memcached-cachestatus-header";
    public final static String MEMCACHED_CACHE_STATUS_HIT_VALUE = "memcached-cachestatus-hit";
    public final static String MEMCACHED_CACHE_STATUS_MISS_VALUE = "memcached-cachestatus-miss";
    public final static String MEMCACHED_STATUS_CODES_TO_CACHE = "memcached-cacheablestatuscodes";

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.PublishToMemcachedFilter.class);

	/**
	 * Has this component been started yet?
	 */

    private volatile int maxContentSizeForMemcachedEntry = DEFAULT_MAX_CACHEABLE_RESPONSE_BODY;

    private DateHeaderFormatter dateHeaderFormatter = new QueueBasedDateFormatter();
    private MemcachedClient client;
    private FilterMemcachedFetching filterMemcachedFetching;
    private FilterMemcachedStorage filterMemcachedStorage;
    private WriteToCacheDecider writeToCacheDecider = new CacheControlWriteToCacheDecider();
    private String cacheHitHeader = CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME;
    private String cacheHitValue = CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE;
    private String cacheMissValue = CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE;

    private volatile boolean isEnabled = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();
        MemcachedKeyConfigBuilder keyConfigBuilder = new MemcachedKeyConfigBuilder();
        String hosts = filterConfig.getInitParameter(MEMCACHED_HOSTS_PARAM);
        builder.setMemcachedHosts(hosts);

        client = builder.build();

        if(client==null) {
            return;
        }


        String key = filterConfig.getInitParameter(MEMCACHED_KEY_PARAM);
        if(key!=null) {
            key = key.trim();
            if(key.length()>0) {
                keyConfigBuilder.setCacheKey(key);
            }
        }

        MemcachedKeyConfig keyConfig = keyConfigBuilder.build();

        MemcachedStorageConfigBuilder storageConfigBuilder = new MemcachedStorageConfigBuilder(keyConfig);

        storageConfigBuilder.setResponseHeadersToIgnore(filterConfig.getInitParameter(MEMCACHED_HEADERS_TO_IGNORE));
        storageConfigBuilder.setMaxHeadersSize(filterConfig.getInitParameter(MEMCACHED_HEADER_SIZE));
        storageConfigBuilder.setStorePrivate(filterConfig.getInitParameter(MEMCACHED_CACHE_PRIVATE));
        storageConfigBuilder.setForceCache(filterConfig.getInitParameter(MEMCACHED_FORCE_CACHE));
        storageConfigBuilder.setForceCacheDuration(filterConfig.getInitParameter(MEMCACHED_FORCE_EXPIRY));
        storageConfigBuilder.setDefaultExpiry(filterConfig.getInitParameter(MEMCACHED_EXPIRY));
        storageConfigBuilder.setCacheableResponseCodes(filterConfig.getInitParameter(MEMCACHED_STATUS_CODES_TO_CACHE));

        MemcachedFetchingConfigBulder fetchingConfigBuilder = new MemcachedFetchingConfigBulder(keyConfig);

        fetchingConfigBuilder.setCacheGetTimeout(filterConfig.getInitParameter(MEMCACHED_GET_TIMEOUT));


        try {
            maxContentSizeForMemcachedEntry = Integer.parseInt(filterConfig.getInitParameter(MEMCACHED_RESPONSE_BODY_SIZE));
        } catch(NumberFormatException e) {

        }

        String cacheHeaderName = filterConfig.getInitParameter(MEMCACHED_CACHE_STATUS_HEADER_NAME);
        if(cacheHeaderName!=null && cacheHeaderName.trim().length()>0) {
            cacheHitHeader = cacheHeaderName.trim();
            storageConfigBuilder.setCacheStatusHeaderName(cacheHitHeader);
        }

        String cacheHitValue = filterConfig.getInitParameter(MEMCACHED_CACHE_STATUS_HIT_VALUE);
        if(cacheHitValue !=null && cacheHitValue.trim().length()>0) {
            this.cacheHitValue = cacheHitValue;
        }

        String cacheMissValue = filterConfig.getInitParameter(MEMCACHED_CACHE_STATUS_MISS_VALUE);
        if(cacheMissValue !=null && cacheMissValue.trim().length()>0) {
            this.cacheMissValue = cacheMissValue;
        }

        filterMemcachedFetching = new SpyFilterMemcachedFetching(client,fetchingConfigBuilder.build());
        filterMemcachedStorage = new SpyFilterMemcachedStorage(client,storageConfigBuilder.build());

        isEnabled = true;
    }

    public void sendCachedResponse(CachedResponse cachedResponse,HttpServletResponse response) {
        Map<String,Collection<String>> headers = cachedResponse.getHeaders();
        for(Map.Entry<String,Collection<String>> header : headers.entrySet()) {
            String key = header.getKey();
            for(String value : header.getValue()) {
                response.addHeader(key, value);
            }
        }

        response.setStatus(cachedResponse.getStatusCode());
        response.addHeader(this.cacheHitHeader,this.cacheHitValue);

        byte[] content = cachedResponse.getContent();
        try {
            response.getOutputStream().write(content,cachedResponse.getContentOffset(),content.length-cachedResponse.getContentOffset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    BufferedResponseWrapper createResponseWrapper(int size,HttpServletResponse originalResponse) {
        return new Servlet2BufferedResponseWrapper(dateHeaderFormatter,size,originalResponse);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(!isEnabled) {
            chain.doFilter(request, response);
        } else {
            BufferedResponseWrapper wrappedRes = null;
            HttpServletRequest servletRequest = null;

            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletResponse servletResponse = (HttpServletResponse) response;
                servletRequest = (HttpServletRequest) request;
                if (CacheConfigGlobals.DEFAULT_REQUEST_METHODS_TO_CACHE.contains(servletRequest.getMethod())) {
                    CachedResponse cacheResponse = filterMemcachedFetching.getCachedContent(servletRequest);
                    if (cacheResponse.isCacheHit()) {
                        sendCachedResponse(cacheResponse, servletResponse);
                        return;
                    } else {
                        wrappedRes = createResponseWrapper(maxContentSizeForMemcachedEntry, servletResponse);
                    }
                }
                servletResponse.addHeader(this.cacheHitHeader,this.cacheMissValue);
            }


            try {
                if (wrappedRes == null) {
                    chain.doFilter(request, response);
                } else {
                    BufferedRequestWrapper requestWrapper = new BufferedRequestWrapper(servletRequest, wrappedRes);
                    chain.doFilter(requestWrapper, wrappedRes);
                }
            } finally {
                if (wrappedRes != null) {
                    postFilter(servletRequest, wrappedRes);
                }
            }
        }
    }

    public void postFilter(HttpServletRequest servletRequest,BufferedResponseWrapper theResponse) {
        storeResponseInMemcached(servletRequest, theResponse);
    }

    void storeResponseInMemcached(HttpServletRequest servletRequest,BufferedResponseWrapper servletResponse) {
        filterMemcachedStorage.writeToCache(servletRequest,servletResponse,writeToCacheDecider);
    }

    @Override
    public void destroy() {
        if(client!=null) {
            client.shutdown();
        }
    }


}
