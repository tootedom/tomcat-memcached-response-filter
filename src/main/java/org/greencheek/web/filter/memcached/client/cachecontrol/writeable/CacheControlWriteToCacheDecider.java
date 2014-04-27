package org.greencheek.web.filter.memcached.client.cachecontrol.writeable;

import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.client.config.MemcachedStorageConfig;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;


/**
 * Created by dominictootell on 27/04/2014.
 */
public class CacheControlWriteToCacheDecider implements WriteToCacheDecider {
    @Override
    public CacheableFor isCacheable(MemcachedStorageConfig config, BufferedResponseWrapper theResponse) {
        if(config.isForceCache()) {
            return new CacheableFor(true,config.getForceCacheDurationInSeconds());
        }
        else {
            String cacheControlHeader = theResponse.getHeader(CacheConfigGlobals.CACHE_CONTROL_HEADER);
            if (cacheControlHeader == null) {
                if (!config.isCanCacheWithNoCacheControlHeader()) {
                    return CacheableFor.NOT_CACHEABLE;
                } else {
                    return new CacheableFor(true,config.getDefaultExpiryInSeconds());
                }
            } else {
                boolean canCache = config.getCacheResponseDecider().isCacheable(cacheControlHeader);
                if (!canCache) {
                    return CacheableFor.NOT_CACHEABLE;
                } else {
                    return new CacheableFor(true,config.getMaxAgeParser().maxAge(cacheControlHeader, config.getDefaultExpiryInSeconds()));
                }
            }
        }
    }
}
