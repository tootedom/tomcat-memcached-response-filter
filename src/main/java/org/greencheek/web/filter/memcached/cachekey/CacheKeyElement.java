package org.greencheek.web.filter.memcached.cachekey;

/**
 * Created by dominictootell on 21/04/2014.
 */
public class CacheKeyElement {
    private String element;
    private boolean available;

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
