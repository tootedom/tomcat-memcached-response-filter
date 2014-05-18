package org.greencheek.web.filter.memcached.client.cachecontrol;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 17/05/2014.
 */
public interface CacheLookupAllowedRule {
    boolean isAllowed(HttpServletRequest request, String cachekey);
}
