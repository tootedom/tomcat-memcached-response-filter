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
    private static final String REMOTE_ADDR = "Remote-Addr";
    private static final String HOST = "Host";

    private static final SplitByChar charSplitter = new CustomSplitByChar();
    private static final JoinByChar charJoiner = new CustomJoinByChar();
    private static final CharSeparatedValueSorter valueSorter = new SplittingCharSeparatedValueSorter(charSplitter,charJoiner);

    private static final Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>() {{
        put(ACCEPT, Arrays.asList("text/plain"));
        put(ACCEPT_ENCODING, Arrays.asList("gzip","deflate"));
        put(CONTENT_LENGTH, Arrays.asList("10"));
        put(CONTENT_TYPE, Arrays.asList("text/plain"));
        put(HOST,null);
    }};

    private static Enumeration<String> toEnumeration(String value) {
        return Collections.enumeration(Collections.singletonList(value));
    }

    private static Enumeration<String> toEnumeration(String... value) {
        return Collections.enumeration(Arrays.asList(value));
    }

    private static List<String> toList(Enumeration<String> values) {
        return Collections.list(values);
    }

    private HttpServletRequest getMockHttpServletRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders(ACCEPT)).thenReturn(Collections.enumeration(headers.get(ACCEPT)));
        when(request.getHeaders(ACCEPT_ENCODING)).thenReturn(Collections.enumeration(headers.get(ACCEPT_ENCODING)));
        when(request.getHeaders(CONTENT_LENGTH)).thenReturn(Collections.enumeration(headers.get(CONTENT_LENGTH)));
        when(request.getHeaders(CONTENT_TYPE)).thenReturn(Collections.enumeration(headers.get(CONTENT_TYPE)));
        when(request.getHeaders(HOST)).thenReturn(null);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(ACCEPT,ACCEPT_ENCODING,CONTENT_LENGTH,CONTENT_TYPE,HOST)));
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
        String headerValues = charJoiner.join(headers.get(ACCEPT_ENCODING),',',32);

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
    public void testRequiredHeaderWithValue() throws Exception {
        String headerValues = charJoiner.join(headers.get(ACCEPT_ENCODING),',',32);

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
    public void testRequiredHeaderWithNoValue() throws Exception {
        String headerValues = charJoiner.join(headers.get(REMOTE_ADDR),',',32);

        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("Remote-Addr",false,
                true, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertFalse(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty string '" + headerValues + "'", headerValues, element.getElement());
    }

    @Test
    public void testRequiredHeaderWithNullValue() throws Exception {
        String headerValues = charJoiner.join(headers.get(HOST),',',32);

        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor(HOST.toLowerCase(),false,
                true, valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertFalse(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty string '" + headerValues + "'", headerValues, element.getElement());
    }


    @Test
    public void testSortedHeaderWithValue() throws Exception {
        String headerValues = valueSorter.sort(charJoiner.join(headers.get(ACCEPT_ENCODING),',',32),',');

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