package org.greencheek.web.filter.memcached.client.config;

/**
 * Created by dominictootell on 16/04/2014.
 */
public class CacheConfigGlobals {
    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String NO_CACHE_CLIENT_VALUE = "no-cache";
    public static final byte[] NEW_LINE = new byte[]{(byte)'\r',(byte)'\n'};
    public static final byte[] HEADER_NAME_SEPARATOR = new byte[]{':',' '};
}
