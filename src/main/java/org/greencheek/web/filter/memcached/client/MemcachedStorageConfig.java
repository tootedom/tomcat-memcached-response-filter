package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 15/04/2014.
 */
public class MemcachedStorageConfig {


    private static final byte[] newLine = new byte[]{(byte)'\r',(byte)'\n'};
    private static final byte[] headerNameSeparator = new byte[]{':',' '};


    private final int headersLength;
    private final CacheKeyCreator cacheKeyCreator;
    private final int defaultExpiryInSeconds;
    private final Map<String,String> responseHeadersToIgnore;


    public MemcachedStorageConfig(int headersLength,CacheKeyCreator cacheKeyCreator,int defaultExpiryInSeconds,
                                  Map<String,String> responseHeadersToIgnore) {
        this.headersLength = headersLength;
        this.cacheKeyCreator = cacheKeyCreator;
        this.defaultExpiryInSeconds = defaultExpiryInSeconds;
        this.responseHeadersToIgnore = responseHeadersToIgnore;
    }


}
