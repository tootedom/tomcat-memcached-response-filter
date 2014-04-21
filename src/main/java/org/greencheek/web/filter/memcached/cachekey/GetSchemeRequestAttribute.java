package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetSchemeRequestAttribute implements GetRequestAttribute {
    public static final GetSchemeRequestAttribute INSTANCE = new GetSchemeRequestAttribute();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        String scheme = request.getScheme();
        return new CacheKeyElement(scheme,true);
    }
}
