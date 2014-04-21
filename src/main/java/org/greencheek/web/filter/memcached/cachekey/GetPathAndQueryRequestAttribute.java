package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetPathAndQueryRequestAttribute implements GetRequestAttribute {
    public static final GetPathAndQueryRequestAttribute INSTANCE = new GetPathAndQueryRequestAttribute();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        return new CacheKeyElement(request.getRequestURI() + request.getQueryString(),true);
    }
}
