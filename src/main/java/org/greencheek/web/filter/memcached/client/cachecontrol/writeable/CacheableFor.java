package org.greencheek.web.filter.memcached.client.cachecontrol.writeable;

/**
 * Created by dominictootell on 27/04/2014.
 */
public class CacheableFor {
    public static final CacheableFor NOT_CACHEABLE = new CacheableFor(false,0);


    private final boolean cacheable;
    private final int durationInSeconds;

    public CacheableFor(boolean canWriteToCache, int durationInSeconds) {
        this.cacheable = canWriteToCache;
        this.durationInSeconds = durationInSeconds;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }
}
