package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.CookieAttributeExtractor;
import org.greencheek.web.filter.memcached.cachekey.extraction.HeaderAttributeExtractor;
import org.greencheek.web.filter.memcached.cachekey.extraction.KeyAttributeExtractor;
import org.greencheek.web.filter.memcached.util.*;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DollarStringKeySpecFactoryTest {


    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final SplitByChar charSplitter = new CustomSplitByChar();
    private static final JoinByChar charJoiner = new CustomJoinByChar();

    private static final Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>() {{
        put(ACCEPT, Arrays.asList("text/plain"));
        put(ACCEPT_ENCODING, Arrays.asList("gzip","deflate"));

    }};

    private KeySpecFactory keySpecFactory;
    private HttpServletRequest mockRequest;

    @Before
    public void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeaders(ACCEPT)).thenReturn(Collections.enumeration(headers.get(ACCEPT)));
        when(mockRequest.getHeaders(ACCEPT_ENCODING)).thenReturn(Collections.enumeration(headers.get(ACCEPT_ENCODING)));
        when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(ACCEPT,ACCEPT_ENCODING)));
        CharSeparatedValueSorter sorter = new SplittingCharSeparatedValueSorter(charSplitter,charJoiner);
        keySpecFactory = new DollarStringKeySpecFactory(charSplitter,sorter);
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
        assertEquals(charJoiner.join(Arrays.asList("gzip","deflate"),',',12),element2.getElement());


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
}