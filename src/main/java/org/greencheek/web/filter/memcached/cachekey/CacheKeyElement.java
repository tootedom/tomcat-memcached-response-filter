package org.greencheek.web.filter.memcached.cachekey;

/**
 * Created by dominictootell on 21/04/2014.
 */
public class CacheKeyElement {
    public static final CacheKeyElement CACHE_KEY_ELEMENT_NOT_AVAILABLE = new CacheKeyElement("",false);
    public static final CacheKeyElement EMPTY_CACHE_KEY_ELEMENT = new CacheKeyElement("",true);

    private final String element;
    private final boolean available;


    public CacheKeyElement(String element, boolean available) {
        this.element = element;
        this.available = available;
    }

    public String getElement() {
        if(element == null) {
            return "";
        }
        return element;
    }

    public boolean isAvailable() {
        return available;
    }
}
