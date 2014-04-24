package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.cachekey.CacheKey;
import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedKeyConfig {
    private final CacheKeyCreator cacheKeyCreator;

    public MemcachedKeyConfig(CacheKeyCreator cacheKeyCreator) {
        this.cacheKeyCreator = cacheKeyCreator;
    }

    /**
     * For the given http request keys the cache key that is to be looked up in memcaced
     * @param theRequest
     * @return
     */
    public String createCacheKey(HttpServletRequest theRequest) {
        return cacheKeyCreator.createCacheKey(theRequest);
    }
}
