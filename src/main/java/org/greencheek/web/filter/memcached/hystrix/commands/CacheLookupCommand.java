package org.greencheek.web.filter.memcached.hystrix.commands;

import com.netflix.hystrix.HystrixCommand;
import org.greencheek.web.filter.memcached.client.FilterMemcachedFetching;
import org.greencheek.web.filter.memcached.domain.CachedResponse;


/**
 * Created by dominictootell on 19/05/2014.
 */
public class CacheLookupCommand extends HystrixCommand<CachedResponse> {

    private static final Exception lookupTimeoutException = new RuntimeException("Timeout on cachelookup");
    private final String cacheKey;
    private final FilterMemcachedFetching cacheLookupImpl;

    public CacheLookupCommand(Setter setter,FilterMemcachedFetching cacheLookup,String cacheKey) {
        super(setter);
        this.cacheKey = cacheKey;
        this.cacheLookupImpl = cacheLookup;
    }

    @Override
    protected CachedResponse run() throws Exception {
        throw lookupTimeoutException;
//        return cacheLookupImpl.getCachedContent(cacheKey);
    }

    @Override
    protected CachedResponse getFallback() {
        return CachedResponse.MISS;
    }
}
