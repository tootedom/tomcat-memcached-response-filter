package org.greencheek.web.filter.memcached.util;

import java.util.Collection;

/**
 * Created by dominictootell on 26/04/2014.
 */
public class CustomJoinByChar implements JoinByChar {


    public String join(Collection<String> values,char c, int expectedLength) {
        if(values==null || values.size()==0) return "";

        StringBuilder b = new StringBuilder(expectedLength);
        for(String value : values) {
            b.append(value).append(c);
        }
        b.setLength(b.length()-1);
        return b.toString();
    }
}
