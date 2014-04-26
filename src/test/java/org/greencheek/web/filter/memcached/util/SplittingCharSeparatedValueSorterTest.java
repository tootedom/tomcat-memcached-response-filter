package org.greencheek.web.filter.memcached.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class SplittingCharSeparatedValueSorterTest {

    private CharSeparatedValueSorter sorter;

    @Before
    public void setUp() {
        sorter = new SplittingCharSeparatedValueSorter(new CustomSplitByChar(),new CustomJoinByChar());
    }

    @Test
    public void testSortEmptyString() {
        assertEquals("",sorter.sort("",','));
    }

}