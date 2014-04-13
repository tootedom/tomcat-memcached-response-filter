package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetPathRequestAttribute implements GetRequestAttribute {
    public static final GetPathRequestAttribute INSTANCE = new GetPathRequestAttribute();
    @Override
    public String getAttribute(HttpServletRequest request, Object... extra) {
        return request.getRequestURI();
    }
}
