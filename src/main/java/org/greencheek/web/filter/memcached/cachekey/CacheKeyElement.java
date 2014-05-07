package org.greencheek.web.filter.memcached.cachekey;

/**
 * Created by dominictootell on 21/04/2014.
 */
public class CacheKeyElement {
    public static final byte[] EMPTY_VALUE = new byte[0];

    public static final CacheKeyElement CACHE_KEY_ELEMENT_NOT_AVAILABLE = new CacheKeyElement(EMPTY_VALUE,false);
    public static final CacheKeyElement EMPTY_CACHE_KEY_ELEMENT = new CacheKeyElement(EMPTY_VALUE,true);

    private final byte[] element;
    private final boolean available;


    public CacheKeyElement(byte[] element, boolean available) {
        this.element = element;
        this.available = available;
    }

    public byte[] getElement() {
        if(element == null) {
            return EMPTY_VALUE;
        }
        return element;
    }

    public boolean isAvailable() {
        return available;
    }
}
