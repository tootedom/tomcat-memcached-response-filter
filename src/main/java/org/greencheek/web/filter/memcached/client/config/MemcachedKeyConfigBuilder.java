package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.DollarStringKeySpecFactory;
import org.greencheek.web.filter.memcached.cachekey.KeySpecFactory;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MemcachedKeyConfigBuilder {

    private KeyHashing keyHashingFunction = CacheConfigGlobals.DEFAULT_MESSAGE_HASHING;
    private String cacheKey = CacheConfigGlobals.DEFAULT_CACHE_KEY;
    private KeySpecFactory keySpecFactory = CacheConfigGlobals.DEFAULT_KEY_SPEC_FACTORY;

    public MemcachedKeyConfig build() {
        return new MemcachedKeyConfig(new DefaultCacheKeyCreator(cacheKey,keyHashingFunction,keySpecFactory));
    }

    public MemcachedKeyConfigBuilder setCacheKey(String cacheKey) {
        if(cacheKey!=null) {
            String key = cacheKey.trim();
            if(key.length()>0) {
                this.cacheKey = cacheKey;
            }
        }
        return this;
    }

    public MemcachedKeyConfigBuilder setKeyHashingFunction(MessageDigestHashing keyHashingFunction) {
        this.keyHashingFunction = keyHashingFunction;
        return this;
    }

    public MemcachedKeyConfigBuilder setKeySpecFactory(KeySpecFactory keySpecFactory) {
        this.keySpecFactory = keySpecFactory;
        return this;
    }
}
