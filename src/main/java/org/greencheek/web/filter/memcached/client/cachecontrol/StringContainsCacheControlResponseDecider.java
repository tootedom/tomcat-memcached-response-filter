package org.greencheek.web.filter.memcached.client.cachecontrol;

/**
 * Created by dominictootell on 19/04/2014.
 */
public class StringContainsCacheControlResponseDecider implements CacheControlResponseDecider {

    private static final String[] CACHE_WITH_PRIVATE = new String[]{"no-cache","no-store","private"};
    private static final String[] CACHE_WITH_NO_PRIVATE = new String[]{"no-cache","no-store"};

    private final String[] stringsToMatch;

    public StringContainsCacheControlResponseDecider(boolean allowPrivate) {
        if(allowPrivate) {
            stringsToMatch = CACHE_WITH_PRIVATE;
        } else {
            stringsToMatch = CACHE_WITH_NO_PRIVATE;
        }
    }

    @Override
    public boolean isCacheable(String cacheControlHeader) {
        for(String matcher : stringsToMatch) {
            if(cacheControlHeader.contains(matcher)) return true;
        }
        return false;
    }
}
