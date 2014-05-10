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
    private final int offset;
    private final int length;

    public CacheKeyElement(byte[] element, boolean available) {
        this(element,0,element.length,available);
    }

    public CacheKeyElement(byte[] element, int offset, int length,boolean available) {
        this.element = element;
        this.offset = offset;
        this.length = length;
        this.available = available;
    }

    public byte[] getElement() {
        if(element == null) {
            return EMPTY_VALUE;
        }
        return element;
    }

    public byte[] getElementCopy() {
        byte[] copy = new byte[length];
        System.arraycopy(element,offset,copy,0,length);
        return copy;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}
