package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public interface GetRequestAttribute {
    public String getAttribute(HttpServletRequest request,Object... extra);
}
