package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.KeyAttributeExtractor;

import java.util.List;

/**
 * Created by dominictootell on 25/04/2014.
 */
public interface KeySpecFactory {
    List<KeyAttributeExtractor> getKeySpecExtractors(String keySpec);
}
