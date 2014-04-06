package org.greencheek.web.filter.memcached.client;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 06/04/2014.
 */
public interface MemcachedClient {
    public void writeToCached(String key, int expiryInSeconds, Set<String> additionalContent,
                              Map<String,Collection<String>> responseHeaders, byte[] content);
}
