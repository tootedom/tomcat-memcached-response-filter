package org.greencheek.web.filter.memcached.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by dominictootell on 22/04/2014.
 */
public class CustomSplitByCharTest {

    private SplitByChar splitter = new CustomSplitByChar();

    @Test
    public void testSplitNoChars() throws Exception {
        String s = "test";
        List<String> items = splitter.split(s,',');
        assertEquals(1,items.size());
        assertEquals("test",items.get(0));
    }

    @Test
    public void testSplitNoValues() throws Exception {
        String s = "";
        List<String> items = splitter.split(s,',');
        assertEquals(0,items.size());
    }

    @Test
    public void testSplitNull() throws Exception {
        String s = null;
        List<String> items = splitter.split(s,',');
        assertEquals(0,items.size());
    }

    @Test
    public void testSplitOneChar() throws Exception {
        String s = "test,test2,test3";
        List<String> items = splitter.split(s,',');
        assertEquals(3,items.size());
        assertEquals("test",items.get(0));
        assertEquals("test2",items.get(1));
        assertEquals("test3",items.get(2));
    }

    @Test
    public void testSplitMultiChar() throws Exception {
        String s = "test,test2,,,,test3";
        List<String> items = splitter.split(s,',');
        assertEquals(3,items.size());
        assertEquals("test",items.get(0));
        assertEquals("test2",items.get(1));
        assertEquals("test3",items.get(2));
    }
}
