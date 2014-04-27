package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.client.cachecontrol.writeable.WriteToCacheDecider;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 06/04/2014.
 */
public interface FilterMemcachedStorage {
    public void writeToCache(HttpServletRequest theRequest, BufferedResponseWrapper theResponse,
                             WriteToCacheDecider cacheDecider);
}
