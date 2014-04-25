package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class MethodAttributeExtractor implements KeyAttributeExtractor {
    public static final MethodAttributeExtractor INSTANCE = new MethodAttributeExtractor();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        return new CacheKeyElement(request.getMethod(),true);
    }
}
