package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class ContentTypeAttributeExtractor implements KeyAttributeExtractor {
    public static final ContentTypeAttributeExtractor INSTANCE = new ContentTypeAttributeExtractor();
    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String contentType = request.getContentType();
        if(contentType==null) {
            contentType ="";
        }
        return new CacheKeyElement(contentType, true);
    }
}
