package org.greencheek.web.filter.memcached.util;

import org.greencheek.web.filter.memcached.util.JoinByChar;

import java.util.List;

/**
 * Created by dominictootell on 26/04/2014.
 */
public class CustomJoinByChar implements JoinByChar {


    public String join(List<String> values,char c, int expectedLength) {
        if(values==null || values.size()==0) return "";

        StringBuilder b = new StringBuilder(expectedLength);
        for(String value : values) {
            b.append(value).append(c);
        }
        b.deleteCharAt(b.length()-1);
        return b.toString();
    }
}
