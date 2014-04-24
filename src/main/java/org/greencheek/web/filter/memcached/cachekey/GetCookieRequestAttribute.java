package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetCookieRequestAttribute implements GetRequestAttribute {
    public static final GetCookieRequestAttribute INSTANCE = new GetCookieRequestAttribute();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        Map<String,String> cookies = (Map<String,String>)extra[0];
        String cookieName = (String)extra[1];
        String cookieValue = cookies.get(cookieName);
        if(cookieValue==null || cookieValue.trim().length()==0) {
            return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
        } else {
            return new CacheKeyElement(cookieValue,true);
        }
    }
}
