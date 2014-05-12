package org.greencheek.web.filter.memcached.util;


/**
 * Created by dominictootell on 12/05/2014.
 */
public interface CacheStatusLogger {

    public void logCacheHit(String cacheKey);
    public void logCacheMiss(String cacheKey);
}
