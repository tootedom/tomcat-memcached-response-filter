package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetMethodRequestAttribute implements GetRequestAttribute {
    public static final GetMethodRequestAttribute INSTANCE = new GetMethodRequestAttribute();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        return new CacheKeyElement(request.getMethod(),true);
    }
}
