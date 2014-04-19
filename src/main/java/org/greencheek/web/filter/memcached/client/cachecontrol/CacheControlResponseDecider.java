package org.greencheek.web.filter.memcached.client.cachecontrol;

/**
 * Created by dominictootell on 19/04/2014.
 */
public interface CacheControlResponseDecider {
    public boolean isCacheable(String cacheControlHeader);
}
