package org.greencheek.web.filter.memcached.util;

import java.util.Collection;
import java.util.List;

/**
 * Created by dominictootell on 26/04/2014.
 */
public interface JoinByChar {
    /**
     * Never Return null.  If the values are null or empty return empty string
     *
     * @param values
     * @param c
     * @param expectedLength
     * @return
     */
    public String join(Collection<String> values,char c, int expectedLength);
}
