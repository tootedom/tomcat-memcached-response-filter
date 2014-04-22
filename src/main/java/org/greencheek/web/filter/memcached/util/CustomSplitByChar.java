package org.greencheek.web.filter.memcached.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dominictootell on 22/04/2014.
 */
public class CustomSplitByChar implements SplitByChar {
    @Override
    public List<String> split(String str, char separatorChar) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        final int len = str.length();
        if (len == 0) {
            return Collections.EMPTY_LIST;
        }
        final List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || lastMatch) {
            list.add(str.substring(start, i));
        }
        return list;
    }
}
