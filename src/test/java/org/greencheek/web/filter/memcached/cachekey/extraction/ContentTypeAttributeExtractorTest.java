package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentTypeAttributeExtractorTest {

    private HttpServletRequest getMockHttpServletRequest() {
        return mock(HttpServletRequest.class);
    }

    @Test
    public void testRequireContentTypeWithNullValue() {
        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getContentType()).thenReturn(null);

        ContentTypeAttributeExtractor extractor = ContentTypeAttributeExtractor.INSTANCE;
        CacheKeyElement element = extractor.getAttribute(request);
        assertTrue(element.isAvailable());
        assertSame(CacheKeyElement.EMPTY_VALUE, element.getElement());

    }

    @Test
    public void testRequireContentTypeWithValue() {
        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getContentType()).thenReturn("text/plain");

        ContentTypeAttributeExtractor extractor = ContentTypeAttributeExtractor.INSTANCE;

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());
        assertArrayEquals("text/plain".getBytes(),element.getElement());
    }

    @Test
    public void testRequireContentTypeNoAvailableValue() {
        HttpServletRequest request =  mock(HttpServletRequest.class);
        ContentTypeAttributeExtractor extractor = ContentTypeAttributeExtractor.INSTANCE;
        CacheKeyElement element = extractor.getAttribute(request);
        assertTrue(element.isAvailable());
        assertSame(CacheKeyElement.EMPTY_VALUE, element.getElement());

    }

}