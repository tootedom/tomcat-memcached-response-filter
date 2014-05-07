package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class PathAndQueryAttributeExtractor implements KeyAttributeExtractor {
    public static final PathAndQueryAttributeExtractor IS_OPTIONAL_INSTANCE = new PathAndQueryAttributeExtractor(true);
    public static final PathAndQueryAttributeExtractor IS_REQUIRED_INSTANCE = new PathAndQueryAttributeExtractor(false);

    private final boolean isOptional;

    public PathAndQueryAttributeExtractor (boolean isOptional) {
        this.isOptional = isOptional;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if(query!=null) {
            query = query.trim();
        } else {
            query = "";
        }
        boolean isEmptyQuery = query.length()==0;

        if(isEmptyQuery && !isOptional) {
            return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
        }

        boolean isEmptyUri;
        if(uri == null) {
            uri = "";
            isEmptyUri = true;
        } else {
            isEmptyUri = uri.length()==0;
        }

        if(isEmptyUri && !isOptional) {
            return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
        }

        if(isEmptyQuery && isEmptyUri) {
            return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
        }

        return new CacheKeyElement(CacheConfigGlobals.getBytes(uri + query),true);
    }
}
