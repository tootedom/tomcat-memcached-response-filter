package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathAndQueryAttributeExtractorTest {


    public HttpServletRequest createRequest(String path, String queryString) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);
        return request;
    }

    @Test
    public void testPathAndQuery() {
        String path = "/context/servlet/restpath";
        String query = "b=a&a=b";
        HttpServletRequest request = createRequest(path,query);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_REQUIRED_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals(path+query,element.getElement());
    }

    @Test
    public void testNullQuery() {
        String path = "/context/servlet/restpath";
        HttpServletRequest request = createRequest(path,null);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals(path,element.getElement());
    }

    @Test
    public void testNoQueryValueAvailable() {
        String path = "/context/servlet/restpath";
        HttpServletRequest request = createRequest(path,"");

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals(path,element.getElement());
    }

    @Test
    public void testNullPath() {
        String query = "b=a&a=b";
        HttpServletRequest request = createRequest(null,query);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals(query,element.getElement());
    }

    @Test
    public void testOptionalNoPathValueAvailable() {
        String query = "b=a&a=b";
        HttpServletRequest request = createRequest("",query);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals(query,element.getElement());
    }

    @Test
    public void testRequiredNullPathValueAvailable() {
        String query = "b=a&a=b";
        HttpServletRequest request = createRequest(null,query);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_REQUIRED_INSTANCE.getAttribute(request);

        assertFalse(element.isAvailable());
        assertSame("",element.getElement());
    }

    @Test
    public void testRequiredNullQueryValueAvailable() {
        String path = "/context/servlet/restpath";
        HttpServletRequest request = createRequest(path,null);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_REQUIRED_INSTANCE.getAttribute(request);

        assertFalse(element.isAvailable());
        assertSame("",element.getElement());
    }

    @Test
    public void testRequiredEmptyQueryValueAvailable() {
        String path = "/context/servlet/restpath";
        HttpServletRequest request = createRequest(path,"");

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_REQUIRED_INSTANCE.getAttribute(request);

        assertFalse(element.isAvailable());
        assertSame("",element.getElement());
    }

    @Test
    public void testNoValues() {
        HttpServletRequest request = createRequest(null,null);

        CacheKeyElement element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertSame("",element.getElement());

        request = createRequest("",null);

        element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals("",element.getElement());

        request = createRequest(null,"");

        element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals("",element.getElement());

        request = createRequest("","");

        element = PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE.getAttribute(request);

        assertTrue(element.isAvailable());
        assertEquals("",element.getElement());
    }
}