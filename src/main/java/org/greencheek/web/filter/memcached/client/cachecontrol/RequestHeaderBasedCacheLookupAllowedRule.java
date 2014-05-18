package org.greencheek.web.filter.memcached.client.cachecontrol;


import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 17/05/2014.
 */
public class RequestHeaderBasedCacheLookupAllowedRule implements CacheLookupAllowedRule {
    public static final RequestHeaderBasedCacheLookupAllowedRule INSTANCE = new RequestHeaderBasedCacheLookupAllowedRule();

    private final String[] noCacheHeaderValues;
    private final String noCacheHeaderName;
    private final boolean hasNoCacheHeaderValues;

    public RequestHeaderBasedCacheLookupAllowedRule() {
        this(CacheConfigGlobals.CACHE_CONTROL_HEADER,CacheConfigGlobals.NO_CACHE_CLIENT_VALUE);
    }

    public RequestHeaderBasedCacheLookupAllowedRule(String[] values) {
        this(CacheConfigGlobals.CACHE_CONTROL_HEADER,values);
    }

    public RequestHeaderBasedCacheLookupAllowedRule(String header, String[] values) {
        this.noCacheHeaderName = header;
        this.noCacheHeaderValues = values;
        if(values==null || values.length==0) {
            hasNoCacheHeaderValues = true;
        } else {
            hasNoCacheHeaderValues = false;
        }
    }

    @Override
    public boolean isAllowed(HttpServletRequest request, String cachekey) {
        if(hasNoCacheHeaderValues) return true;
        if(cachekey==null) return false;
        String cacheControlHeader = request.getHeader(noCacheHeaderName);
        if(cacheControlHeader == null) return true;

        for(String val : noCacheHeaderValues) {
            if(cacheControlHeader.contains(val)) return false;
        }
        return true;
    }
}
