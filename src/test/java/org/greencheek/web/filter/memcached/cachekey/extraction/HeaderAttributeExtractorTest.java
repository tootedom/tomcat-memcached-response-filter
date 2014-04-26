package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.util.*;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HeaderAttributeExtractorTest {

    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";

    private static final SplitByChar charSplitter = new CustomSplitByChar();
    private static final JoinByChar charJoiner = new CustomJoinByChar();
    private static final CharSeparatedValueSorter valueSorter = new SplittingCharSeparatedValueSorter(charSplitter,charJoiner);

    private static final Map<String,Enumeration<String>> headers = new HashMap<String,Enumeration<String>>() {{
        put(ACCEPT, toEnumeration("text/plain"));
        put(ACCEPT_ENCODING, toEnumeration("gzip","deflate"));
        put(CONTENT_LENGTH, toEnumeration("10"));
        put(CONTENT_TYPE, toEnumeration("text/plain"));
    }};

    private static Enumeration<String> toEnumeration(String value) {
        return Collections.enumeration(Collections.singletonList(value));
    }

    private static Enumeration<String> toEnumeration(String... value) {
        return Collections.enumeration(Arrays.asList(value));
    }

    private HttpServletRequest getMockHttpServletRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders(ACCEPT)).thenReturn(headers.get(ACCEPT));
        when(request.getHeaders(ACCEPT_ENCODING)).thenReturn(headers.get(ACCEPT_ENCODING));
        when(request.getHeaders(CONTENT_LENGTH)).thenReturn(headers.get(CONTENT_LENGTH));
        when(request.getHeaders(CONTENT_TYPE)).thenReturn(headers.get(CONTENT_TYPE));
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(ACCEPT,ACCEPT_ENCODING,CONTENT_LENGTH,CONTENT_TYPE)));
        return request;
    }

    @Test
    public void testOptionalHeaderWithNoValue() {
        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("content-types",true,
                false, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty string", "", element.getElement());
    }

    @Test
    public void testOptionalHeaderWithValue() throws Exception {
        Enumeration<String> headerValues = headers.get(ACCEPT_ENCODING);

        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("accept-encoding",true,
                false, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertEquals("Header should be string '" + headerValues + "'", headerValues, element.getElement());
    }

    @Test
    public void testSortedHeaderWithValue() throws Exception {
        String headerValues = "deflate,gzip";

        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("accept-encoding",true,
                true, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertEquals("Header should be string '" + headerValues + "'", headerValues, element.getElement());
    }

    @Test
    public void testSortedHeaderWithSingleValue() throws Exception {
        String headerValues = "text/plain";

        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("accept",true,
                true, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertEquals("Header should be string '"+headerValues+"'",headerValues,element.getElement());
    }
}