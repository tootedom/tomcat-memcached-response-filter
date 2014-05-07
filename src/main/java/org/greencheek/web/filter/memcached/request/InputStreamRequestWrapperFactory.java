package org.greencheek.web.filter.memcached.request;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 06/05/2014.
 */
public interface InputStreamRequestWrapperFactory {

    HttpServletRequest createRequestWrapper(HttpServletRequest originalRequest,
                                            boolean requiresContent,
                                            int maxContentLength);
}
