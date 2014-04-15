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
import java.util.*;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.greencheek.web.filter.memcached.client.FilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.spy.SpyFilterMemcachedStorage;
import org.greencheek.web.filter.memcached.client.spy.SpyMemcachedBuilder;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
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


    private final FilterMemcachedStorage filterMemcachedStorage = new SpyFilterMemcachedStorage(new SpyMemcachedBuilder().build());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        BufferedResponseWrapper wrappedRes = null;
        HttpServletRequest servletRequest = null;
        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletRequest = (HttpServletRequest) request;
            wrappedRes = new BufferedResponseWrapper(maxContentSizeForMemcachedEntry,servletResponse);
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

        ResizeableByteBuffer bufferedContent = servletResponse.getBufferedMemcachedContent();
        boolean shouldWriteToMemcached = bufferedContent.canWrite();
        bufferedContent.closeForWrites();
        if(bufferedContent!=null && shouldWriteToMemcached) {
          String key = createKey(servletRequest);
          filterMemcachedStorage.writeToCache(key, 10, Collections.EMPTY_SET, getHeaders(DEFAULT_HEADERS_TO_IGNORE, servletResponse), servletResponse.getBufferedMemcachedContent());
        }

    }

    private Map<String,Collection<String>> getHeaders(Set<String> headerNamesToIngore,BufferedResponseWrapper servletResponse) {
        Collection<String> headerNames = servletResponse.getHeaderNames();
        Map<String,Collection<String>> headers = new HashMap<String, Collection<String>>(headerNames.size());

        for(String key : headerNames) {
            if(headerNamesToIngore.contains(key.toLowerCase())) continue;
            headers.put(key,servletResponse.getHeaders(key));
        }
        return headers;
    }

    private String createKey(HttpServletRequest servletRequest) {
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
