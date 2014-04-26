package org.greencheek.web.filter.memcached.util;

import java.util.List;

/**
 * Created by dominictootell on 22/04/2014.
 */
public interface SplitByChar {
    /**
     * Return an empty list if string is null or empty
     */
    List<String> split(String string,char character);
}
