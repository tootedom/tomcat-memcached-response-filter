package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class SchemeAttributeExtractor implements KeyAttributeExtractor {
    public static final SchemeAttributeExtractor INSTANCE = new SchemeAttributeExtractor();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        return new CacheKeyElement(CacheConfigGlobals.getASCIIBytes(request.getScheme()),true);
    }
}
