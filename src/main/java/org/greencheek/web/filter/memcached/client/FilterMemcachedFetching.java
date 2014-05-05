package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.domain.CachedResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 14/04/2014.
 */
public interface FilterMemcachedFetching {
    public CachedResponse getCachedContent(HttpServletRequest request, String key);
}
