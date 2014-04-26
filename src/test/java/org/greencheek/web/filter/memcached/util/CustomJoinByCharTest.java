package org.greencheek.web.filter.memcached.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CustomJoinByCharTest {

    @Test
    public void testSingleItem() {
        CustomJoinByChar joiner = new CustomJoinByChar();
        assertEquals("one", joiner.join(Collections.singletonList("one"), ',', 3));
    }


    @Test
    public void testNoItem() {
        CustomJoinByChar joiner = new CustomJoinByChar();
        assertSame("",joiner.join(Collections.EMPTY_LIST, ',', 3));
    }

    @Test
    public void testNull() {
        CustomJoinByChar joiner = new CustomJoinByChar();
        assertSame("",joiner.join(null, ',', 3));
    }


    @Test
    public void testTwoItems() {
        CustomJoinByChar joiner = new CustomJoinByChar();
        assertEquals("one,two",joiner.join(Arrays.asList("one","two"),',',8));
    }


    @Test
    public void testThreeItems() {
        CustomJoinByChar joiner = new CustomJoinByChar();
        assertEquals("one,two,three",joiner.join(Arrays.asList("one","two","three"),',',13));
    }
}