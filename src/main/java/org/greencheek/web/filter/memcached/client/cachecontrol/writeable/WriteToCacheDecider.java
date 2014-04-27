package org.greencheek.web.filter.memcached.client.cachecontrol.writeable;

import org.greencheek.web.filter.memcached.client.config.MemcachedStorageConfig;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;


/**
 * Created by dominictootell on 27/04/2014.
 */
public interface WriteToCacheDecider {
    public CacheableFor isCacheable(MemcachedStorageConfig config,
                                    BufferedResponseWrapper response);

}
