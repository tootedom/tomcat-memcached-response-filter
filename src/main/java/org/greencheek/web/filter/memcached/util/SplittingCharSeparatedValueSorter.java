package org.greencheek.web.filter.memcached.util;

import java.util.Collections;
import java.util.List;

/**
 * Created by dominictootell on 26/04/2014.
 */
public class SplittingCharSeparatedValueSorter implements CharSeparatedValueSorter {
    private final SplitByChar charSplitter;
    private final JoinByChar charJoiner;

    public SplittingCharSeparatedValueSorter(SplitByChar charSplitter, JoinByChar joinByChar) {
        this.charSplitter = charSplitter;
        this.charJoiner = joinByChar;
    }

    @Override
    public String sort(String value, char separator) {
        int length = value==null ? 0 : value.length();

        List<String> valuesToSort = charSplitter.split(value,separator);
        Collections.sort(valuesToSort);
        return charJoiner.join(valuesToSort,separator,length+valuesToSort.size());
    }
}
