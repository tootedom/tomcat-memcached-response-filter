package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 06/04/2014.
 */
public interface FilterMemcachedStorage {
    public void writeToCache(String key, int expiryInSeconds, Set<String> additionalStaticHeaders,
                             Map<String, Collection<String>> responseHeaders, ResizeableByteBuffer content);
}
