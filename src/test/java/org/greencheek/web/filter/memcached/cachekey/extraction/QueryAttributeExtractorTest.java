package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryAttributeExtractorTest {

    @Test
    public void testNullQueryString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn(null);
        CacheKeyElement element = QueryAttributeExtractor.INSTANCE.getAttribute(request);
        assertTrue(element.isAvailable());
        assertSame("", element.getElement());
    }

    @Test
    public void testQueryString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn("?bob=bob&p=p");
        CacheKeyElement element = QueryAttributeExtractor.INSTANCE.getAttribute(request);
        assertTrue(element.isAvailable());
        assertEquals("?bob=bob&p=p", element.getElement());
    }
}