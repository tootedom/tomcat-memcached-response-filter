package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodAttributeExtractorTest {

    @Test
    public void testGetAttribute() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        CacheKeyElement element = MethodAttributeExtractor.INSTANCE.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("GET".getBytes(),element.getElement());
    }
}