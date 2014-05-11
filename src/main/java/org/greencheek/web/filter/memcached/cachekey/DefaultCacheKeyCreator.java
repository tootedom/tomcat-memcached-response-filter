package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class DefaultCacheKeyCreator implements CacheKeyCreator {

    private static final Logger log = LoggerFactory.getLogger(org.greencheek.web.filter.memcached.cachekey.DefaultCacheKeyCreator.class);

    private final KeyHashing keyHashingUtil;
    private final List<KeyAttributeExtractor> extractors;
    private final int estimatedKeySize;
    private final int maxCacheKeySize;

    public DefaultCacheKeyCreator(String keySpec, KeyHashing keyHashingUtil,
                                  KeySpecFactory keySpecFactory) {
        this(CacheConfigGlobals.DEFAULT_ESTIMATED_CACHED_KEY_SIZE,
             CacheConfigGlobals.DEFAULT_MAX_CACHE_KEY_SIZE,
             keySpec,keyHashingUtil,keySpecFactory);
    }


    public DefaultCacheKeyCreator(int estimatedKeySize,int maxCacheKeySize,String keySpec,
                                  KeyHashing keyHashingUtil,
                                  KeySpecFactory keySpecFactory) {
        this.maxCacheKeySize = maxCacheKeySize;
        this.keyHashingUtil = keyHashingUtil;
        extractors = keySpecFactory.getKeySpecExtractors(keySpec);
        if(estimatedKeySize==-1 || estimatedKeySize<1) {
            this.estimatedKeySize = CacheConfigGlobals.DEFAULT_ESTIMATED_KEY_INDIVIDUAL_ITEM_SIZE * extractors.size();
        } else {
            this.estimatedKeySize = estimatedKeySize;
        }

    }

    @Override
    public String createCacheKey(HttpServletRequest request) {
        ResizeableByteBuffer b = new ResizeableByteBuffer(estimatedKeySize,maxCacheKeySize);

        for(KeyAttributeExtractor extractor : extractors) {
            CacheKeyElement keyElement = extractor.getAttribute(request);
            if(!keyElement.isAvailable()) {
                return null;
            }
            b.append(keyElement.getElement(),keyElement.getOffset(),keyElement.getLength());
        }

        if(b.canWrite()) {
            return keyHashingUtil.hash(b.getBuf(), 0, b.position());
        } else {
            log.debug("{\"method\":\"createCacheKey\",\"message\":\"Unable to create cache key.  Max Cache Key size reached: {}\"}",maxCacheKeySize);
            return null;
        }
    }







}
