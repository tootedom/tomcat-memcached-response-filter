package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class PathAttributeExtractor implements KeyAttributeExtractor {
    public static final PathAttributeExtractor INSTANCE = new PathAttributeExtractor();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        return new CacheKeyElement(CacheConfigGlobals.getBytes(request.getRequestURI()),true);
    }
}
