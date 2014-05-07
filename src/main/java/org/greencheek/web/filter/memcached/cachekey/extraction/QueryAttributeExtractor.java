package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class QueryAttributeExtractor implements KeyAttributeExtractor {
    public static final QueryAttributeExtractor IS_REQUIRED_INSTANCE = new QueryAttributeExtractor(false);
    public static final QueryAttributeExtractor IS_OPTIONAL_INSTANCE = new QueryAttributeExtractor(true);

    private final boolean isOptional;

    public QueryAttributeExtractor(boolean optional) {
        isOptional = optional;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String query = request.getQueryString();
        if(query == null || query.length()==0) {
            if(isOptional) {
                return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
            } else {
                return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
            }
        }
        return new CacheKeyElement(CacheConfigGlobals.getBytes(query),true);
    }
}
