package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetPathAndQueryRequestAttribute implements GetRequestAttribute {
    public static final GetPathAndQueryRequestAttribute INSTANCE = new GetPathAndQueryRequestAttribute();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if(query == null) query = "";
        if(uri == null) uri = "";
        return new CacheKeyElement(uri + query,true);
    }
}
