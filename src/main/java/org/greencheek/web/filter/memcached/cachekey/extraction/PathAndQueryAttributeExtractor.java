package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class PathAndQueryAttributeExtractor implements KeyAttributeExtractor {
    public static final PathAndQueryAttributeExtractor INSTANCE = new PathAndQueryAttributeExtractor();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if(query == null && uri == null) {
            return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
        } else if(query == null) {
            query = "";
        } else if(uri == null) {
            uri = "";
        }
        return new CacheKeyElement(uri + query,true);
    }
}
