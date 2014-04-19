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
import java.util.Map;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.greencheek.web.filter.memcached.client.*;
import org.greencheek.web.filter.memcached.client.config.*;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedFetching;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedBuilder;
import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.response.BufferedRequestWrapper;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PublishToMemcachedFilter implements Filter {


	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.PublishToMemcachedFilter.class);

	/**
	 * Has this component been started yet?
	 */
	protected boolean started = false; 


    private volatile int maxContentSizeForMemcachedEntry = 8192*4;

    private final MemcachedClient client = new SpyMemcachedBuilder().build();
    private final MemcachedKeyConfig keyConfig = new MemcachedKeyConfigBuilder().build();
    private final FilterMemcachedFetching filterMemcachedFetching = new SpyFilterMemcachedFetching(client,new MemcachedFetchingConfigBulder(keyConfig).build());
    private final FilterMemcachedStorage filterMemcachedStorage = new SpyFilterMemcachedStorage(client,new MemcachedStorageConfigBuilder(keyConfig).build());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void sendCachedResponse(CachedResponse cachedResponse,HttpServletResponse response) {
        Map<String,String> headers = cachedResponse.getHeaders();
        for(Map.Entry<String,String> header : headers.entrySet()) {
            response.addHeader(header.getKey(),header.getValue());
        }

        response.setStatus(cachedResponse.getStatusCode());
        response.addHeader("X-Cache","HIT");

        byte[] content = cachedResponse.getContent();
        try {
            response.getOutputStream().write(content,cachedResponse.getContentOffset(),content.length-cachedResponse.getContentOffset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        BufferedResponseWrapper wrappedRes = null;
        HttpServletRequest servletRequest = null;

        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletRequest = (HttpServletRequest) request;
            if(CacheConfigGlobals.DEFAULT_REQUEST_METHODS_TO_CACHE.contains(servletRequest.getMethod())) {
                CachedResponse cacheResponse = filterMemcachedFetching.getCachedContent(servletRequest);
                if(cacheResponse.isCacheHit())
                {
                    sendCachedResponse(cacheResponse,servletResponse);
                    return;
                }
                else {
                    wrappedRes = new BufferedResponseWrapper(maxContentSizeForMemcachedEntry, servletResponse);
                }
            }
        }

        try {
            if (wrappedRes == null) {
                chain.doFilter(request, response);
            } else {
                BufferedRequestWrapper requestWrapper = new BufferedRequestWrapper(servletRequest,wrappedRes);
                chain.doFilter(requestWrapper, wrappedRes);
            }
        } finally {
            if(wrappedRes!=null) {
                final AsyncContext asyncContext = servletRequest.getAsyncContext();
                if(asyncContext!=null) {
                    asyncContext.addListener(new SetInMemcachedListener(servletRequest, wrappedRes));
                } else {
                    storeResponseInMemcached(servletRequest, wrappedRes);
                }
            }
        }
    }

    private void storeResponseInMemcached(HttpServletRequest servletRequest,BufferedResponseWrapper servletResponse) {
        filterMemcachedStorage.writeToCache(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }

    class SetInMemcachedListener implements AsyncListener {

        private final HttpServletRequest request;
        private final BufferedResponseWrapper responseWrapper;
        private volatile boolean error = false;

        public SetInMemcachedListener(HttpServletRequest servletRequest, BufferedResponseWrapper servletResponse) {
            this.request = servletRequest;
            this.responseWrapper = servletResponse;
        }

        @Override
        public void onComplete(AsyncEvent asyncEvent) throws IOException {
            if (!error) {
                storeResponseInMemcached(request, responseWrapper);
            }
        }

        @Override
        public void onTimeout(AsyncEvent asyncEvent) throws IOException {
            error = true;
            onComplete(asyncEvent);
        }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {
            error = true;
            onComplete(asyncEvent);
        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        }
    }
}
