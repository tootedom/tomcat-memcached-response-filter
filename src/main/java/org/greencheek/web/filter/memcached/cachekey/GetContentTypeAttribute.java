package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetContentTypeAttribute implements GetRequestAttribute {
    public static final GetContentTypeAttribute INSTANCE = new GetContentTypeAttribute();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        return new CacheKeyElement(request.getContentType(),true);
    }
}
