package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.io.ByteArrayBasedServletInputStream;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestBodyAttributeExtractorTest {

    private ServletInputStream inputStream;
    private byte[] body;

    @Before
    public void setUp() {
        try {
            body = "hellothere".getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            body = "hellothere".getBytes();
        }
        inputStream = new ByteArrayBasedServletInputStream(body);
    }

    @Test
    public void testContentExtractionForPost() {
        testContentExtractionHasBody("POST");
    }

    @Test
    public void testContentExtractionForPut() {
        testContentExtractionHasBody("PUT");
    }

    @Test
    public void testContentExtractionForGet() {
        testContentExtractionHasNoBody("GET");
    }

    private void testContentExtractionHasBody(String method) {

        CacheKeyElement element = testContentExtraction(method);
        assertTrue(element.isAvailable());
        assertArrayEquals(body, element.getElement());
    }

    private void testContentExtractionHasNoBody(String method) {

        CacheKeyElement element = testContentExtraction(method);
        assertTrue(element.isAvailable());
        assertArrayEquals(new byte[0], element.getElement());
    }

    private CacheKeyElement testContentExtraction(String method) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        try {
            when(request.getInputStream()).thenReturn(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RequestBodyAttributeExtractor.INSTANCE.getAttribute(request);
    }

}