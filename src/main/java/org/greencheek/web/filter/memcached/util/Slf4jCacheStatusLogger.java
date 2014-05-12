package org.greencheek.web.filter.memcached.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dominictootell on 12/05/2014.
 */
public class Slf4jCacheStatusLogger implements CacheStatusLogger {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.util.CacheStatusLogger.class);

    @Override
    public void logCacheHit(String cacheKey) {
        logCacheStatus("HIT",cacheKey);
    }

    @Override
    public void logCacheMiss(String cacheKey) {
        logCacheStatus("MISS",cacheKey);
    }

    private void logCacheStatus(String status,String cacheKey) {
        log.info("{\"cachestatus\":\"{}\",\"key\":\"{}\"}",status,cacheKey);

    }
}
