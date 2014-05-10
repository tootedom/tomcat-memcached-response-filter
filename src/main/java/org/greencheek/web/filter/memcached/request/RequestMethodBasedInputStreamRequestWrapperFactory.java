package org.greencheek.web.filter.memcached.request;

import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 06/05/2014.
 */
public class RequestMethodBasedInputStreamRequestWrapperFactory implements InputStreamRequestWrapperFactory {
    @Override
    public HttpServletRequest createRequestWrapper(HttpServletRequest originalRequest, boolean requiresContent,
                                                   int initialContentBufferSize,int maxContentLength) {
        if(requiresContent && CacheConfigGlobals.METHODS_WITH_CONTENT.contains(originalRequest.getMethod())) {
            int length = originalRequest.getContentLength();
            if(length == -1 || length>maxContentLength ) {
                return null;
            }
            else {
                return new MultiReadRequestWrapper(initialContentBufferSize,length,originalRequest);
            }
        }
        else {
            return originalRequest;
        }
    }
}
