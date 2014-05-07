package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemeAttributeExtractorTest {

    @Test
    public void testPathExtraction() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        CacheKeyElement element = SchemeAttributeExtractor.INSTANCE.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("http".getBytes(), element.getElement());
    }
}