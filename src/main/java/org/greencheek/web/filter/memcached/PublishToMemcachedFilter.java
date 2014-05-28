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
import org.greencheek.web.filter.memcached.client.cachecontrol.CacheLookupAllowedRule;
import org.greencheek.web.filter.memcached.client.cachecontrol.RequestHeaderBasedCacheLookupAllowedRule;
import org.greencheek.web.filter.memcached.client.cachecontrol.writeable.CacheControlWriteToCacheDecider;
import org.greencheek.web.filter.memcached.client.cachecontrol.writeable.WriteToCacheDecider;
import org.greencheek.web.filter.memcached.client.config.*;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedBuilder;
import org.greencheek.web.filter.memcached.dateformatting.DateHeaderFormatter;
import org.greencheek.web.filter.memcached.dateformatting.QueueBasedDateFormatter;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.request.BufferedRequestWrapper;
import org.greencheek.web.filter.memcached.request.RequestMethodBasedInputStreamRequestWrapperFactory;
import org.greencheek.web.filter.memcached.request.InputStreamRequestWrapperFactory;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;
import org.greencheek.web.filter.memcached.response.Servlet2BufferedResponseWrapper;
import org.greencheek.web.filter.memcached.util.CacheStatusLogger;
import org.greencheek.web.filter.memcached.util.Slf4jCacheStatusLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PublishToMemcachedFilter implements Filter {

    public final static String MEMCACHED_HOSTS_PARAM = "memcached-hosts";
    public final static String MEMCACHED_KEY_PARAM = "memcached-key";
    public final static String MEMCACHED_HEADERS_TO_IGNORE = "memcached-ignore-headers";
    public final static String MEMCACHED_RESPONSE_BODY_SIZE = "memcached-maxcacheable-bodysize";
    public final static String MEMCACHED_RESPONSE_BODY_INITIAL_SIZE = "memcached-initialcacheable-bodysize";
    public final static String MEMCACHED_RESPONSE_MAX_HEADER_SIZE = "memcached-response-max-header-size";
    public final static String MEMCACHED_RESPONSE_ESTIMATED_HEADER_SIZE = "memcached-response-estimated-header-size";
    public final static String MEMCACHED_GET_TIMEOUT = "memcached-get-timeout-millis";
    public final static String MEMCACHED_CACHE_PRIVATE = "memcached-cache-private";
    public final static String MEMCACHED_FORCE_CACHE = "memcached-force-cache";
    public final static String MEMCACHED_EXPIRY = "memcached-expiry";
    public final static String MEMCACHED_FORCE_EXPIRY = "memcached-forced-expiry";
    public final static String MEMCACHED_CACHE_STATUS_HEADER_NAME = "memcached-cachestatus-header";
    public final static String MEMCACHED_CACHE_STATUS_HIT_VALUE = "memcached-cachestatus-hit";
    public final static String MEMCACHED_CACHE_STATUS_MISS_VALUE = "memcached-cachestatus-miss";
    public final static String MEMCACHED_STATUS_CODES_TO_CACHE = "memcached-cacheablestatuscodes";
    public final static String MEMCACHED_CACHE_WITH_NO_CACHE_CONTROL = "memcached-cache-nocachecontrol";
    public final static String MEMCACHED_KEY_HASHING_PARAM = "memcached-key-hashing";
    public final static String MEMCACHED_CACHEABLE_METHODS = "memcached-cacheable-methods";
    public final static String MEMCACHED_CHECK_HOST_CONNECTIVITY = "memcached-checkhost-connectivity";
    public final static String MEMCACHED_DNS_TIMEOUT = "memcached-host-dnsresolutiontimeout-secs";
    public final static String MEMCACHED_USE_BINARY = "memcached-use-binary-protocol";
    public final static String MEMCACHED_MAX_POST_BODY_SIZE = "memcached-max-post-body-size";
    public final static String MEMCACHED_INITIAL_POST_BODY_SIZE = "memcached-initial-post-body-size";
    public final static String MEMCACHED_MAX_CACHE_KEY_SIZE = "memcached-max-cache-key-size";
    public final static String MEMCACHED_ESTIMATED_CACHE_KEY_SIZE = "memcached-estimated-cache-key-size";
    public final static String MEMCACHED_NODE_FAILURE_MODE = "memcached-failure-mode";
    public final static String MEMCACHED_FILTER_ENABLED = "memcached-filter-enabled";
    public final static String MEMCACHED_NO_CACHE_REQUEST_HEADER = "memcached-nocache-request-header";
    public final static String MEMCACHED_NO_CACHE_REQUEST_HEADER_VALUES = "memcached-nocache-request-header-values";
    public final static String MEMCACHED_HASH_ALGORITHM = "memcached-hash-algorithm";


	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.PublishToMemcachedFilter.class);

	/**
	 * Has this component been started yet?
	 */

    private static final CacheStatusLogger cacheStatusLogger = new Slf4jCacheStatusLogger();
    private int maxContentSizeForMemcachedEntry;
    private int initialContentSizeForMemcachedEntry;
    private DateHeaderFormatter dateHeaderFormatter = new QueueBasedDateFormatter();
    private MemcachedClient client;
    private FilterMemcachedFetching filterMemcachedFetching;
    private CacheLookupAllowedRule cacheLookupRuler;
    private FilterMemcachedStorage filterMemcachedStorage;
    private MemcachedKeyConfig keyConfig;
    private CacheableMethods cacheableMethods;
    private WriteToCacheDecider writeToCacheDecider = new CacheControlWriteToCacheDecider();
    private String cacheHitHeader = CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME;
    private String cacheHitValue = CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE;
    private String cacheMissValue = CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE;
    private boolean requiresContent = false;
    private InputStreamRequestWrapperFactory requestWrapperFactory = new RequestMethodBasedInputStreamRequestWrapperFactory();
    private int maxPostBodySize = CacheConfigGlobals.DEFAULT_MAX_POST_BODY_SIZE;
    private int initialPostBodySize = CacheConfigGlobals.DEFAULT_INITIAL_POST_BODY_SIZE;
    private volatile boolean isEnabled = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        boolean isEnabled = CacheConfigGlobals.parseBoolValue(filterConfig.getInitParameter(MEMCACHED_FILTER_ENABLED),true);
        if(!isEnabled) {
            this.isEnabled = false;
            return;
        }

        cacheLookupRuler = new RequestHeaderBasedCacheLookupAllowedRule(
                CacheConfigGlobals.parseStringValue(filterConfig.getInitParameter(MEMCACHED_NO_CACHE_REQUEST_HEADER),CacheConfigGlobals.CACHE_CONTROL_HEADER),
                CacheConfigGlobals.parseCommaSeparatedList(filterConfig.getInitParameter(MEMCACHED_NO_CACHE_REQUEST_HEADER_VALUES),CacheConfigGlobals.NO_CACHE_CLIENT_VALUE)
        );

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();
        MemcachedKeyConfigBuilder keyConfigBuilder = new MemcachedKeyConfigBuilder();
        builder.setHashAlgorithm(filterConfig.getInitParameter(MEMCACHED_HASH_ALGORITHM));
        keyConfigBuilder.setKeyHashingFunction(filterConfig.getInitParameter(MEMCACHED_KEY_HASHING_PARAM));
        keyConfigBuilder.setMaxCacheKeySize(CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_MAX_CACHE_KEY_SIZE),
                CacheConfigGlobals.DEFAULT_MAX_CACHE_KEY_SIZE));
        keyConfigBuilder.setEstimatedCacheKeySize(CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_ESTIMATED_CACHE_KEY_SIZE),
                CacheConfigGlobals.DEFAULT_ESTIMATED_CACHED_KEY_SIZE));

        boolean checkHostsConnectivity = Boolean.parseBoolean(filterConfig.getInitParameter(MEMCACHED_CHECK_HOST_CONNECTIVITY));
        builder.setCheckHostConnectivity(checkHostsConnectivity);
        builder.setDNSTimeoutInSeconds(filterConfig.getInitParameter(MEMCACHED_DNS_TIMEOUT));

        String listOfMethods = filterConfig.getInitParameter(MEMCACHED_CACHEABLE_METHODS);
        if(listOfMethods == null || listOfMethods.trim().length()==0) {
            cacheableMethods = new CommaSeparatedCacheableMethods("GET",CacheConfigGlobals.DEFAULT_CHAR_SPLITTER);
        } else {
            cacheableMethods = new CommaSeparatedCacheableMethods(listOfMethods,CacheConfigGlobals.DEFAULT_CHAR_SPLITTER);
        }

        String hosts = filterConfig.getInitParameter(MEMCACHED_HOSTS_PARAM);
        builder.setMemcachedHosts(hosts);
        builder.setUseBinaryProtocol(Boolean.parseBoolean(filterConfig.getInitParameter(MEMCACHED_USE_BINARY)));

        builder.setFailureMode(filterConfig.getInitParameter(MEMCACHED_NODE_FAILURE_MODE));


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

        keyConfig = keyConfigBuilder.build();
        requiresContent = keyConfigBuilder.requiresBody();
        maxPostBodySize = CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_MAX_POST_BODY_SIZE),CacheConfigGlobals.DEFAULT_MAX_POST_BODY_SIZE);
        initialPostBodySize = CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_INITIAL_POST_BODY_SIZE),CacheConfigGlobals.DEFAULT_INITIAL_POST_BODY_SIZE);

        MemcachedStorageConfigBuilder storageConfigBuilder = new MemcachedStorageConfigBuilder();

        storageConfigBuilder.setResponseHeadersToIgnore(filterConfig.getInitParameter(MEMCACHED_HEADERS_TO_IGNORE));
        storageConfigBuilder.setEstimatedHeaderSize(CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_RESPONSE_ESTIMATED_HEADER_SIZE), CacheConfigGlobals.DEFAULT_RESPONSE_ESTIMATED_HEADER_SIZE));
        storageConfigBuilder.setMaxHeadersSize(CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_RESPONSE_MAX_HEADER_SIZE), CacheConfigGlobals.DEFAULT_RESPONSE_MAX_HEADERS_LENGTH_TO_STORE));
        storageConfigBuilder.setStorePrivate(filterConfig.getInitParameter(MEMCACHED_CACHE_PRIVATE));
        storageConfigBuilder.setForceCache(filterConfig.getInitParameter(MEMCACHED_FORCE_CACHE));
        storageConfigBuilder.setForceCacheDuration(filterConfig.getInitParameter(MEMCACHED_FORCE_EXPIRY));
        storageConfigBuilder.setDefaultExpiry(filterConfig.getInitParameter(MEMCACHED_EXPIRY));
        storageConfigBuilder.setCacheableResponseCodes(filterConfig.getInitParameter(MEMCACHED_STATUS_CODES_TO_CACHE));
        storageConfigBuilder.setCanCacheWithNoCacheControl(filterConfig.getInitParameter(MEMCACHED_CACHE_WITH_NO_CACHE_CONTROL));

        MemcachedFetchingConfigBulder fetchingConfigBuilder = new MemcachedFetchingConfigBulder();

        fetchingConfigBuilder.setCacheGetTimeout(filterConfig.getInitParameter(MEMCACHED_GET_TIMEOUT));


        maxContentSizeForMemcachedEntry = CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_RESPONSE_BODY_SIZE), CacheConfigGlobals.DEFAULT_MAX_CACHEABLE_RESPONSE_BODY);
        initialContentSizeForMemcachedEntry = CacheConfigGlobals.parseIntValue(filterConfig.getInitParameter(MEMCACHED_RESPONSE_BODY_INITIAL_SIZE), CacheConfigGlobals.DEFAULT_INITIAL_CACHEABLE_RESPONSE_BODY);

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

        this.isEnabled = true;
    }

    public FilterMemcachedFetching getMemcachedFetchingImpl() {
        return filterMemcachedFetching;
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
            log.error("Unable to send cached response to client",e);
        }
    }

    BufferedResponseWrapper createResponseWrapper(int initalSize,int maxSize,HttpServletResponse originalResponse, String cacheKey) {
        return new Servlet2BufferedResponseWrapper(dateHeaderFormatter,initalSize,maxSize,originalResponse, cacheKey);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(!isEnabled) {
            log.debug("{\"method\":\"doFilter\",\"message\":\"memcached filter is not enabled\"}");
            chain.doFilter(request, response);
        } else {
            BufferedResponseWrapper wrappedRes = null;
            HttpServletRequest servletRequest = null;
            String cacheKey = null;
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletResponse servletResponse = (HttpServletResponse) response;
                servletRequest = (HttpServletRequest) request;
                if (cacheableMethods.isCacheable(servletRequest)) {
                    HttpServletRequest wrappedRequest = requestWrapperFactory.createRequestWrapper(servletRequest,requiresContent,initialPostBodySize, maxPostBodySize);
                    if(wrappedRequest!=null) {
                        servletRequest = wrappedRequest;
                        cacheKey = keyConfig.createCacheKey(servletRequest);
                    }
                }

                if(cacheKey!=null && cacheKey.length()>0) {
                    CachedResponse cacheResponse;
                    if(cacheLookupAllowed(servletRequest, cacheKey)) {
                        cacheResponse = executeCacheLookup(cacheKey);
                    } else {
                        cacheResponse = CachedResponse.MISS;
                    }

                    if (cacheResponse.isCacheHit()) {
                        cacheStatusLogger.logCacheHit(cacheKey);
                        sendCachedResponse(cacheResponse, servletResponse);
                        return;
                    } else {
                        wrappedRes = createResponseWrapper(initialContentSizeForMemcachedEntry,maxContentSizeForMemcachedEntry, servletResponse,cacheKey);
                    }
                }
                cacheStatusLogger.logCacheMiss(cacheKey);
                servletResponse.addHeader(this.cacheHitHeader,this.cacheMissValue);
            }


            try {
                if (wrappedRes == null) {
                    chain.doFilter(request, response);
                } else {
                    BufferedRequestWrapper requestWrapper = new BufferedRequestWrapper(servletRequest, wrappedRes);
                    doBackEndRequest(chain,cacheKey,requestWrapper,wrappedRes);
                }
            } finally {
                if (wrappedRes != null) {
                    postFilter(servletRequest, wrappedRes);
                }
            }
        }
    }

    protected boolean cacheLookupAllowed(HttpServletRequest request,String cacheKey) {
        return cacheLookupRuler.isAllowed(request,cacheKey);
    }

    /**
     * Execute cache lookup
     * @param cacheKey
     */
    public CachedResponse executeCacheLookup(String cacheKey) {
        return filterMemcachedFetching.getCachedContent(cacheKey);
    }

    /**
     * Performs the backend request.
     *
     * @param chain
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doBackEndRequest(FilterChain chain,String cacheKey,HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        chain.doFilter(request, response);
    }

    /**
     * Post the backend request's completion
     * @param servletRequest
     * @param theResponse
     */
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
