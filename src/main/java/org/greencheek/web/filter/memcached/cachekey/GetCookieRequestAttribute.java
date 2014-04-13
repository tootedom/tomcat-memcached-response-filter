package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetCookieRequestAttribute implements GetRequestAttribute {
    public static final GetCookieRequestAttribute INSTANCE = new GetCookieRequestAttribute();

    @Override
    public String getAttribute(HttpServletRequest request, Object... extra) {
        Map<String,String> cookies = (Map<String,String>)extra[0];
        String cookieName = (String)extra[1];
        return cookies.get(cookieName);
    }
}
