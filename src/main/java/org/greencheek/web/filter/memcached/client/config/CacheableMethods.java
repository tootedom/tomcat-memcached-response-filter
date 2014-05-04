package org.greencheek.web.filter.memcached.client.config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 04/05/2014.
 */
public interface CacheableMethods {
    public boolean isCacheable(HttpServletRequest request);
}
