package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator;
import org.greencheek.web.filter.memcached.cachekey.KeySpecFactory;
import org.greencheek.web.filter.memcached.keyhashing.FastestXXHashKeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.JavaXXHashKeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;

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

    public boolean requiresBody() {
        return keySpecFactory.requiresBody(this.cacheKey);
    }

    public MemcachedKeyConfigBuilder setKeyHashingFunction(String hashingFunction) {
        if(hashingFunction==null || hashingFunction.trim().length()==0) {
            keyHashingFunction = CacheConfigGlobals.DEFAULT_MESSAGE_HASHING;
        }
        else if(hashingFunction.equalsIgnoreCase("javaxxhash") || hashingFunction.equalsIgnoreCase("xxhash")) {
            keyHashingFunction = new JavaXXHashKeyHashing();
        }
        else if(hashingFunction.equalsIgnoreCase("nativexxhash")) {
            keyHashingFunction = new FastestXXHashKeyHashing();
        } else if(hashingFunction.equalsIgnoreCase("md5")) {
            keyHashingFunction = new MessageDigestHashing(KeyHashing.MD5);
        } else if(hashingFunction.equalsIgnoreCase("sha")) {
            keyHashingFunction = new MessageDigestHashing(KeyHashing.SHA526);
        } else {
            keyHashingFunction = CacheConfigGlobals.DEFAULT_MESSAGE_HASHING;
        }
        return this;
    }

    public MemcachedKeyConfigBuilder setKeyHashingFunction(KeyHashing keyHashingFunction) {
        this.keyHashingFunction = keyHashingFunction;
        return this;
    }

    public MemcachedKeyConfigBuilder setKeySpecFactory(KeySpecFactory keySpecFactory) {
        this.keySpecFactory = keySpecFactory;
        return this;
    }
}
