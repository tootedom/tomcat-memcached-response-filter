package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetHeaderRequestAttribute implements GetRequestAttribute {

    public static final GetHeaderRequestAttribute INSTANCE = new GetHeaderRequestAttribute();

    @Override
    public String getAttribute(HttpServletRequest request, Object... extra) {
        return request.getHeader(extra[0].toString().toLowerCase());
    }
}
