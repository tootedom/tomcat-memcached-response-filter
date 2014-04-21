package org.greencheek.web.filter.memcached.cachekey;

/**
 * Created by dominictootell on 21/04/2014.
 */
public class CacheKey {
    private final boolean fullyPopulated;
    private final String key;

    public CacheKey(boolean fullKey, String key) {
        this.fullyPopulated = fullKey;
        this.key = key;
    }

    public boolean isFullyPopulated() {
        return fullyPopulated;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return key;
    }
}
