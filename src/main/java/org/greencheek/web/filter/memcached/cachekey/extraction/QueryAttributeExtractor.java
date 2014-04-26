package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class QueryAttributeExtractor implements KeyAttributeExtractor {
    public static final QueryAttributeExtractor INSTANCE = new QueryAttributeExtractor();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String query = request.getQueryString();
        if(query==null) {
            query = "";
        }
        return new CacheKeyElement(query,true);
    }
}
