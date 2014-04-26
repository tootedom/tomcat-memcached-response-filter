package org.greencheek.web.filter.memcached.util;

import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void testSortNullString() {
        assertEquals("",sorter.sort(null,','));
    }

    @Test
    public void testSortString() {
        assertEquals("a,b",sorter.sort("b,a",','));
    }

    @Test
    public void testSortStringDifferentChar() {
        assertEquals("a;b",sorter.sort("b;a",';'));
    }

    @Test
    public void testSortStringWrongChar() {
        assertEquals("b,a",sorter.sort("b,a",';'));
    }

    @Test
    public void testStringNoChar() {
        assertEquals("a",sorter.sort("a",','));
    }

    @Test
    public void testStringWithMultipleItems() {
        assertEquals("a,a,b,c,f,z",sorter.sort("a,c,f,b,a,z",','));
    }
}