package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.util.*;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class DollarStringKeySpecFactoryTest {


    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String VALUE_METHOD = "GET";
    private static final String VALUE_SCHEME = "http";
    private static final String VALUE_URI = "/context/servlet/rest";
    private static final String VALUE_CONTENT_TYPE = "text/plain";
    private static final String VALUE_QUERY = "";

    private static final SplitByChar charSplitter = new CustomSplitByChar();
    private static final JoinByChar charJoiner = new CustomJoinByChar();
    private static final CharSeparatedValueSorter sorter = new SplittingCharSeparatedValueSorter(charSplitter,charJoiner);


    private static final Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>() {{
        put(ACCEPT, Arrays.asList("text/plain"));
        put(ACCEPT_ENCODING, Arrays.asList("gzip","deflate"));

    }};

    private KeySpecFactory keySpecFactory;
    private HttpServletRequest mockRequest;
    private HttpServletRequest mockRequestMissingAcceptEncoding;

    @Before
    public void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        mockRequestMissingAcceptEncoding = mock(HttpServletRequest.class);
        when(mockRequest.getHeaders(ACCEPT)).thenReturn(Collections.enumeration(headers.get(ACCEPT)));
        when(mockRequest.getHeaders(ACCEPT_ENCODING)).thenReturn(Collections.enumeration(headers.get(ACCEPT_ENCODING)));
        when(mockRequest.getMethod()).thenReturn(VALUE_METHOD);
        when(mockRequest.getScheme()).thenReturn(VALUE_SCHEME);
        when(mockRequest.getContentType()).thenReturn(VALUE_CONTENT_TYPE);
        when(mockRequest.getRequestURI()).thenReturn(VALUE_URI);
        when(mockRequest.getQueryString()).thenReturn(VALUE_QUERY);

        when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(ACCEPT,ACCEPT_ENCODING)));
        keySpecFactory = new DollarStringKeySpecFactory(charSplitter,sorter);

        when(mockRequestMissingAcceptEncoding.getHeaders(ACCEPT)).thenReturn(Collections.enumeration(headers.get(ACCEPT)));
        when(mockRequestMissingAcceptEncoding.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(ACCEPT,ACCEPT_ENCODING)));
    }

    @Test
    public void testOptionalCookie() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$cookie_jsessionid?");
        assertEquals(1, extractors.size());

        assertTrue(extractors.get(0) instanceof CookieAttributeExtractor);

        CookieAttributeExtractor extractor = (CookieAttributeExtractor)extractors.get(0);

        CacheKeyElement element = extractor.getAttribute(mock(HttpServletRequest.class));

        assertTrue(element.isAvailable());
    }

    @Test
    public void testRequiredCookie() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$cookie_jsessionid");
        assertEquals(1, extractors.size());

        assertTrue(extractors.get(0) instanceof CookieAttributeExtractor);

        CookieAttributeExtractor extractor = (CookieAttributeExtractor)extractors.get(0);

        CacheKeyElement element = extractor.getAttribute(mock(HttpServletRequest.class));

        assertFalse(element.isAvailable());
    }

    @Test
    public void testHeaders() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$header_accept$header_accept-encoding$header_unknown?$header_required-unknown");
        assertEquals(4, extractors.size());

        assertTrue(extractors.get(0) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor1 = (HeaderAttributeExtractor)extractors.get(0);
        assertTrue(extractors.get(1) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor2 = (HeaderAttributeExtractor)extractors.get(1);

        CacheKeyElement element = extractor1.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals("text/plain",element.getElement());

        CacheKeyElement element2 = extractor2.getAttribute(mockRequest);
        assertTrue(element2.isAvailable());
        assertEquals(charJoiner.join(Arrays.asList("gzip", "deflate"), ',', 12),element2.getElement());


        assertTrue(extractors.get(2) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor3 = (HeaderAttributeExtractor)extractors.get(2);
        assertTrue(extractors.get(3) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor4 = (HeaderAttributeExtractor)extractors.get(3);


        element = extractor3.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals("",element.getElement());

        element = extractor4.getAttribute(mockRequest);
        assertFalse(element.isAvailable());
        assertEquals("",element.getElement());

    }

    @Test
    public void testSortedHeaders() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$header_accept$header_accept-encoding_s?$header_unknown?$header_required-unknown");
        assertEquals(4, extractors.size());

        assertTrue(extractors.get(1) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor2 = (HeaderAttributeExtractor)extractors.get(1);

        CacheKeyElement element2 = extractor2.getAttribute(mockRequest);
        assertTrue(element2.isAvailable());
        assertEquals(sorter.sort(charJoiner.join(Arrays.asList("gzip", "deflate"), ',', 12), ','),element2.getElement());
    }

    @Test
    public void testRequiredSortedHeaders() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$header_accept$header_accept-encoding_s$header_unknown?$header_required-unknown");
        assertEquals(4, extractors.size());

        assertTrue(extractors.get(1) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor2 = (HeaderAttributeExtractor)extractors.get(1);

        CacheKeyElement element2 = extractor2.getAttribute(mockRequestMissingAcceptEncoding);
        assertFalse(element2.isAvailable());
        assertEquals("",element2.getElement());
    }

    @Test
    public void testOptionalSortedHeaders() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$header_accept$header_accept-encoding_s?$header_unknown?$header_required-unknown");
        assertEquals(4, extractors.size());

        assertTrue(extractors.get(1) instanceof HeaderAttributeExtractor);
        HeaderAttributeExtractor extractor2 = (HeaderAttributeExtractor)extractors.get(1);

        CacheKeyElement element2 = extractor2.getAttribute(mockRequestMissingAcceptEncoding);
        assertTrue(element2.isAvailable());
        assertEquals("",element2.getElement());
    }

    @Test
    public void testMissingHeaderValue() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$header_");
        assertEquals(0, extractors.size());

        extractors =  keySpecFactory.getKeySpecExtractors("$header");
        assertEquals(0, extractors.size());
    }

    @Test
    public void testMissingCookieValue() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$cookie_");
        assertEquals(0, extractors.size());

        extractors =  keySpecFactory.getKeySpecExtractors("$cookie");
        assertEquals(0, extractors.size());
    }

    @Test
    public void testSchemeMethodUriQueryArgsAndRequestURI() {
        List<KeyAttributeExtractor> extractors =  keySpecFactory.getKeySpecExtractors("$scheme$request_method$uri$query?$args$request_uri?$request_uri$");
        assertEquals(7, extractors.size());

        assertTrue(extractors.get(0) instanceof SchemeAttributeExtractor);
        SchemeAttributeExtractor extractor1 = (SchemeAttributeExtractor)extractors.get(0);

        assertTrue(extractors.get(1) instanceof MethodAttributeExtractor);
        MethodAttributeExtractor extractor2 = (MethodAttributeExtractor)extractors.get(1);

        assertTrue(extractors.get(2) instanceof PathAttributeExtractor);
        PathAttributeExtractor extractor3 = (PathAttributeExtractor)extractors.get(2);

        assertTrue(extractors.get(3) instanceof QueryAttributeExtractor);
        QueryAttributeExtractor extractor4 = (QueryAttributeExtractor)extractors.get(3);

        assertTrue(extractors.get(4) instanceof QueryAttributeExtractor);
        QueryAttributeExtractor extractor5 = (QueryAttributeExtractor)extractors.get(4);

        assertTrue(extractors.get(5) instanceof PathAndQueryAttributeExtractor);
        PathAndQueryAttributeExtractor extractor6 = (PathAndQueryAttributeExtractor)extractors.get(5);

        assertTrue(extractors.get(6) instanceof PathAndQueryAttributeExtractor);
        PathAndQueryAttributeExtractor extractor7 = (PathAndQueryAttributeExtractor)extractors.get(6);

        CacheKeyElement element = extractor1.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals(VALUE_SCHEME,element.getElement());

        element = extractor2.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals(VALUE_METHOD,element.getElement());

        element = extractor3.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals(VALUE_URI,element.getElement());

        element = extractor4.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals(VALUE_QUERY,element.getElement());

        element = extractor5.getAttribute(mockRequest);
        assertFalse(element.isAvailable());
        assertEquals(VALUE_QUERY,element.getElement());

        element = extractor6.getAttribute(mockRequest);
        assertTrue(element.isAvailable());
        assertEquals(VALUE_URI,element.getElement());

        element = extractor7.getAttribute(mockRequest);
        assertFalse(element.isAvailable());
        assertEquals("",element.getElement());
    }
}