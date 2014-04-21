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
    private final KeyHashing keyHashing;

    public MemcachedKeyConfig(CacheKeyCreator cacheKeyCreator,final KeyHashing keyHashing) {
        this.cacheKeyCreator = cacheKeyCreator;
        this.keyHashing = keyHashing;
    }

    /**
     * For the given http request keys the cache key that is to be looked up in memcaced
     * @param theRequest
     * @return
     */
    public CacheKey createCacheKey(HttpServletRequest theRequest) {
        CacheKey key = cacheKeyCreator.createCacheKey(theRequest);
        return new CacheKey(key.isFullyPopulated(),keyHashing.hash(key.getKey()));
    }
}
