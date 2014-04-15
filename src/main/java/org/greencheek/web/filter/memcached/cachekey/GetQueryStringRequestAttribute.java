package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetQueryStringRequestAttribute implements GetRequestAttribute {
    public static final GetQueryStringRequestAttribute INSTANCE = new GetQueryStringRequestAttribute();

    @Override
    public String getAttribute(HttpServletRequest request, Object... extra) {
        return request.getQueryString();
    }
}