package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedKeyConfigBuilder {
    public static final String DEFAULT_CACHE_KEY = "$scheme$request_method$request_uri$header_accept$header_accept-encoding";
    public static final KeyHashing DEFAULT_MESSAGE_HASHING = new MessageDigestHashing();

    private CacheKeyCreator cacheKeyCreator = new DefaultCacheKeyCreator(DEFAULT_CACHE_KEY);
    private KeyHashing keyHashingFunction = DEFAULT_MESSAGE_HASHING;


    public MemcachedKeyConfig build() {
        return new MemcachedKeyConfig(cacheKeyCreator,keyHashingFunction);
    }

    public MemcachedKeyConfigBuilder setCacheKey(String cacheKey) {
        this.cacheKeyCreator = new DefaultCacheKeyCreator(cacheKey);
        return this;
    }

    public MemcachedKeyConfigBuilder setKeyHashingFunction(MessageDigestHashing keyHashingFunction) {
        this.keyHashingFunction = keyHashingFunction;
        return this;
    }
}
