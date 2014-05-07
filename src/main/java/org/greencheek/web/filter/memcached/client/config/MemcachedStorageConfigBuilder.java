package org.greencheek.web.filter.memcached.client.config;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.greencheek.web.filter.memcached.client.cachecontrol.CacheControlResponseDecider;
import org.greencheek.web.filter.memcached.client.cachecontrol.DefaultMaxAgeParser;
import org.greencheek.web.filter.memcached.client.cachecontrol.MaxAgeParser;
import org.greencheek.web.filter.memcached.client.cachecontrol.StringContainsCacheControlResponseDecider;
import org.greencheek.web.filter.memcached.domain.Duration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfigBuilder {
    public static int DEFAULT_MAX_HEADERS_LENGTH_TO_STORE = 8192;
    public static Set<String> DEFAULT_ADDITIONAL_HEADERS = Collections.EMPTY_SET;
    public static final int DEFAULT_EXPIRY_IN_SECONDS = 300;
    public static final boolean DEFAULT_STORE_PRIVATE = false;
    public static final boolean DEFAULT_FORCE_CACHE = false;
    public static final int DEFAULT_FORCE_CACHE_DURATION = DEFAULT_EXPIRY_IN_SECONDS;
    public static final byte[] DEFAULT_HTTP_STATUS_LINE = new byte[]{'H','T','T','P','/','1','.','1',' '};
    public static final MaxAgeParser DEFAULT_MAX_AGE_PARSER = new DefaultMaxAgeParser();
    public static final boolean DEFAULT_CAN_CACHE_WITH_NO_CACHE_CONTROL = true;
    public static final CacheControlResponseDecider DEFAULT_CACHE_RESPONSE_DECIDER = new StringContainsCacheControlResponseDecider(DEFAULT_STORE_PRIVATE);


    private static final Set<String> DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    static {
        Set<String> headers = new HashSet<String>(9);
        headers.add("connection");
        headers.add("keep-alive");
        headers.add("proxy-authenticate");
        headers.add("proxy-authorization");
        headers.add("te");
        headers.add("trailers");
        headers.add("transfer-encoding");
        headers.add("upgrade");
        headers.add("set-cookie");
        headers.add("date");
        DEFAULT_RESPONSE_HEADERS_TO_IGNORE = Collections.unmodifiableSet(headers);
    }




    private Set<String> responseHeadersToIgnore = DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    private int defaultExpiryInSeconds = DEFAULT_EXPIRY_IN_SECONDS;
    private Set<String> additionalHeaders  = DEFAULT_ADDITIONAL_HEADERS;
    private int defaultMaxHeadersLengthToStore = DEFAULT_MAX_HEADERS_LENGTH_TO_STORE;
    private boolean storePrivate = DEFAULT_STORE_PRIVATE;
    private boolean forceCache = DEFAULT_FORCE_CACHE;
    private int forceCacheDuration = DEFAULT_FORCE_CACHE_DURATION;
    private byte[] httpStatusLinePrefix = DEFAULT_HTTP_STATUS_LINE;
    private MaxAgeParser maxAgeParser = DEFAULT_MAX_AGE_PARSER;
    private boolean canCacheWithNoCacheControl = DEFAULT_CAN_CACHE_WITH_NO_CACHE_CONTROL;
    private CacheControlResponseDecider cacheResponseDecider = DEFAULT_CACHE_RESPONSE_DECIDER;
    private TIntSet cacheableResponseCodes = CacheConfigGlobals.CACHEABLE_RESPONSE_CODES;
    private String cacheStatusHeaderName = CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME;




    public MemcachedStorageConfigBuilder()
    {
    }

    public MemcachedStorageConfig build() {
        return new MemcachedStorageConfig(defaultMaxHeadersLengthToStore,defaultExpiryInSeconds,
                additionalHeaders,responseHeadersToIgnore,cacheResponseDecider,forceCache,
                forceCacheDuration,httpStatusLinePrefix,maxAgeParser,canCacheWithNoCacheControl,
                cacheableResponseCodes,cacheStatusHeaderName);
    }


    public MemcachedStorageConfigBuilder setDefaultExpiry(Duration duration) {
        defaultExpiryInSeconds = (int)duration.toSeconds();
        return this;
    }

    public MemcachedStorageConfigBuilder setDefaultExpiry(String expiryInSeconds) {
        if (expiryInSeconds == null) return this;

        expiryInSeconds = expiryInSeconds.trim();
        if (expiryInSeconds.length() > 0) {
            try {
                this.defaultExpiryInSeconds = (int)new Duration(Integer.parseInt(expiryInSeconds), TimeUnit.SECONDS).toSeconds();
            } catch (NumberFormatException e) {

            }
        }
        return this;
    }

    public MemcachedStorageConfigBuilder setMaxHeadersSize(int lengthInBytes) {
        this.defaultMaxHeadersLengthToStore = lengthInBytes;
        return this;
    }

    public MemcachedStorageConfigBuilder setMaxHeadersSize(String lengthInBytes) {

        try {
            this.defaultMaxHeadersLengthToStore = Integer.parseInt(lengthInBytes);
        } catch(NumberFormatException e) {

        }
        return this;
    }

    public MemcachedStorageConfigBuilder setResponseHeaderToIgnore(Set<String> headersToIgnore) {
        this.responseHeadersToIgnore = new HashSet<String>(headersToIgnore);
        return this;
    }

    public MemcachedStorageConfigBuilder setResponseHeadersToIgnore(String headers) {
        if(headers==null) return this;
        String[] headerList =  headers.split(",");

        Set<String> headersToIgnore = new HashSet<String>(headerList.length);
        for(String header : headerList) {
            String s = header.trim();
            if(s.length()>0) {
                headersToIgnore.add(s);
            }
        }

        this.responseHeadersToIgnore = headersToIgnore;
        return this;
    }


    public MemcachedStorageConfigBuilder setResponseHeaderToIgnore(String[] headersToIgnore) {
        this.responseHeadersToIgnore = new HashSet<String>(headersToIgnore.length);
        for(String header : headersToIgnore) {
            responseHeadersToIgnore.add(header);
        }
        return this;
    }

    public MemcachedStorageConfigBuilder setAdditionalCustomHeaders(Set<String> customHeaders) {
        this.additionalHeaders = new HashSet<String>(customHeaders);
        return this;
    }

    public MemcachedStorageConfigBuilder setAdditionalCustomHeaders(String[] customHeaders) {
        this.additionalHeaders = new HashSet<String>(customHeaders.length);
        for(String customHeader : customHeaders) {
            additionalHeaders.add(customHeader);
        }
        return this;
    }

    public MemcachedStorageConfigBuilder setStorePrivate(String storePrivate) {
        if(storePrivate==null || storePrivate.trim().length()==0) {
            return this;
        }
        this.cacheResponseDecider = new StringContainsCacheControlResponseDecider(Boolean.parseBoolean(storePrivate));
        return this;
    }

    public MemcachedStorageConfigBuilder setStorePrivate(boolean storePrivate) {
        this.cacheResponseDecider = new StringContainsCacheControlResponseDecider(storePrivate);
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCache(boolean forceCache) {
        this.forceCache = forceCache;
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCache(String forceCache) {
        this.forceCache = Boolean.parseBoolean(forceCache);
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCacheDuration(Duration duration) {
        this.forceCacheDuration = (int)duration.toSeconds();
        return this;
    }

    public MemcachedStorageConfigBuilder setForceCacheDuration(String expiryInSeconds) {
        if (expiryInSeconds == null) return this;

        expiryInSeconds = expiryInSeconds.trim();
        if (expiryInSeconds.length() > 0) {
            try {
                this.forceCacheDuration = (int)new Duration(Integer.parseInt(expiryInSeconds), TimeUnit.SECONDS).toSeconds();
            } catch (NumberFormatException e) {

            }
        }
        return this;
    }


    public MemcachedStorageConfigBuilder setHttpStatusLinePrefix(String prefix) {
        prefix = prefix.trim();
        this.httpStatusLinePrefix = CacheConfigGlobals.getBytes(prefix);
        return this;
    }


    public MemcachedStorageConfigBuilder setMaxAgeParser(MaxAgeParser maxAgeParser) {
        this.maxAgeParser = maxAgeParser;
        return this;
    }

    public MemcachedStorageConfigBuilder setCanCacheWithNoCacheControl(String canCacheWithNoCacheControl) {
        if(canCacheWithNoCacheControl == null || canCacheWithNoCacheControl.trim().length()==0) {
            return this;
        } else {
            this.canCacheWithNoCacheControl = Boolean.parseBoolean(canCacheWithNoCacheControl);
        }
        return this;
    }

    public MemcachedStorageConfigBuilder setCanCacheWithNoCacheControl(boolean canCacheWithNoCacheControl) {
        this.canCacheWithNoCacheControl = canCacheWithNoCacheControl;
        return this;
    }

    public MemcachedStorageConfigBuilder setCacheableResponseCodes(String codes) {
        if(codes==null) {
            return this;
        }
        codes = codes.trim();
        if(codes.length()==0) return this;

        TIntSet statusCodes = CacheConfigGlobals.commaSeparatedIntStringToIntSet(codes);

        if(statusCodes.size()==0) return this;
        this.cacheableResponseCodes = statusCodes;
        return this;
    }

    public MemcachedStorageConfigBuilder setCacheableResponseCodes(int... statusCodes) {
        cacheableResponseCodes = new TIntHashSet(statusCodes.length,1.0f);
        cacheableResponseCodes.addAll(statusCodes);
        return this;
    }

    public MemcachedStorageConfigBuilder setCacheStatusHeaderName(String name) {
        cacheStatusHeaderName = name;
        return this;
    }

}
