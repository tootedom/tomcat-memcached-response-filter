package org.greencheek.web.filter.memcached.request;

import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 06/05/2014.
 */
public class RequestMethodBasedInputStreamRequestWrapperFactory implements InputStreamRequestWrapperFactory {
    @Override
    public HttpServletRequest createRequestWrapper(HttpServletRequest originalRequest, boolean requiresContent,
                                                   int contentLength) {
        if(requiresContent && CacheConfigGlobals.METHODS_WITH_CONTENT.contains(originalRequest.getMethod())) {
            int length = originalRequest.getContentLength();
            if(length == -1 || length>contentLength ) {
                return null;
            }
            else {
                return new MultiReadRequestWrapper(originalRequest);
            }
        }
        else {
            return originalRequest;
        }
    }
}
