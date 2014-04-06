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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.greencheek.web.filter.memcached.client.MemcachedClient;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedBuilder;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedClient;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferWithOverflowMarker;
import org.greencheek.web.filter.memcached.response.BufferedRequestWrapper;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;


public class PublishToMemcachedFilter implements Filter {

	/**
	 * Logger
	 */
	private static final Log log = LogFactory.getLog(org.greencheek.web.filter.memcached.PublishToMemcachedFilter.class);

	/**
	 * Has this component been started yet?
	 */
	protected boolean started = false; 


    private volatile int maxContentSizeForMemcachedEntry = 8192*4;


    private final MemcachedClient memcachedClient = new SpyMemcachedClient(new SpyMemcachedBuilder().build());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        BufferedResponseWrapper wrappedRes = null;
        HttpServletRequest servletRequest = null;
        boolean isAsync = false;
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletRequest = (HttpServletRequest) request;
            wrappedRes = new BufferedResponseWrapper(maxContentSizeForMemcachedEntry,servletResponse);
        }

        try {
            // record access for non ignored paths
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

        ResizeableByteBufferWithOverflowMarker bufferedContent = servletResponse.getBufferedMemcachedContent();

        if(bufferedContent!=null && !bufferedContent.hasOverflowed()) {
          String key = createKey(servletRequest,servletResponse);
          memcachedClient.writeToCached(key,10, Collections.EMPTY_SET,getHeaders(servletResponse.getHeaderNames(),servletResponse),servletResponse.getBufferedMemcachedContent().toByteArray());
        }

    }

    private Map<String,Collection<String>> getHeaders(Collection<String> headerNames,BufferedResponseWrapper servletResponse) {
        Map<String,Collection<String>> headers = new HashMap<String, Collection<String>>(headerNames.size());

        for(String key : headerNames) {
            headers.put(key,servletResponse.getHeaders(key));
        }
        return headers;
    }

    private String createKey(HttpServletRequest servletRequest,BufferedResponseWrapper servletResponse) {
        String method = servletRequest.getMethod();
        String path = servletRequest.getRequestURI();
        String queryString = servletRequest.getQueryString();

        StringBuilder requestedResource = null;
        // Reconstruct original requesting URL
        if (queryString != null) {
            requestedResource = new StringBuilder(path.length() + queryString.length() + 1);
            requestedResource.append(path);
            requestedResource.append('?');
            requestedResource.append(queryString);
        } else {
            requestedResource = new StringBuilder(path.length());
            requestedResource.append(path);
        }

        return requestedResource.toString();
    }

    @Override
    public void destroy() {

    }

    class SetInMemcachedListener implements AsyncListener {

        private final HttpServletRequest request;
        private final BufferedResponseWrapper responseWrapper;
        private boolean error = false;

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
            onComplete(asyncEvent);
        }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {
            onComplete(asyncEvent);
        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        }
    }
}