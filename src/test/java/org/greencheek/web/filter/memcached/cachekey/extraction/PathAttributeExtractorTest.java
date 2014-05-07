package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathAttributeExtractorTest {

    @Test
    public void testPathExtraction() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/url/path");
        CacheKeyElement element = PathAttributeExtractor.INSTANCE.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("/url/path".getBytes(), element.getElement());
    }
}