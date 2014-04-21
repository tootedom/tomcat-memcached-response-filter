package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetPathRequestAttribute implements GetRequestAttribute {
    public static final GetPathRequestAttribute INSTANCE = new GetPathRequestAttribute();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        return new CacheKeyElement(request.getRequestURI(),true);
    }
}
