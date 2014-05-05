package org.greencheek.web.filter.memcached;


import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;
import org.greencheek.web.filter.memcached.response.Servlet2BufferedResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 19/04/2014.
 */
public class Servlet3PublishToMemcachedFilter extends PublishToMemcachedFilter {

    @Override
    public void postFilter(HttpServletRequest servletRequest, BufferedResponseWrapper theResponse) {
        final AsyncContext asyncContext = servletRequest.getAsyncContext();
        if (asyncContext != null) {
            asyncContext.addListener(new SetInMemcachedListener(servletRequest, theResponse));
        } else {
            super.storeResponseInMemcached(servletRequest, theResponse);
        }
    }

    @Override
    BufferedResponseWrapper createResponseWrapper(int size,HttpServletResponse originalResponse, String cacheKey) {
        return new BufferedResponseWrapper(size,originalResponse,cacheKey);
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
