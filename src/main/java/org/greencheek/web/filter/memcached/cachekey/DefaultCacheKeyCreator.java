package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class DefaultCacheKeyCreator implements CacheKeyCreator {

    private final KeyHashing keyHashingUtil;
    private final List<KeyAttributeExtractor> extractors;
    private final int estimatedKeySize;



    public DefaultCacheKeyCreator(String keySpec, KeyHashing keyHashingUtil,
                                  KeySpecFactory keySpecFactory) {
        this.keyHashingUtil = keyHashingUtil;
        extractors = keySpecFactory.getKeySpecExtractors(keySpec);
        estimatedKeySize = 32 * extractors.size();

    }

    @Override
    public String createCacheKey(HttpServletRequest request) {
        ResizeableByteBuffer b = new ResizeableByteBuffer(estimatedKeySize,ResizeableByteBuffer.MAX_ARRAY_SIZE);
//        StringBuilder b = new StringBuilder(estimatedKeySize);

        for(KeyAttributeExtractor extractor : extractors) {
            CacheKeyElement keyElement = extractor.getAttribute(request);
            if(!keyElement.isAvailable()) {
                return null;
            }
            b.append(keyElement.getElement());
        }

        return keyHashingUtil.hash(b.getBuf(),0,b.position());
    }







}
