package org.greencheek.web.filter.memcached.cachekey.extraction;


import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.io.ByteArrayBasedServletInputStream;
import org.greencheek.web.filter.memcached.util.*;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParamsAttributeExtractorTest {



    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HOST = "Host";
    private static final String POST_BODY = "four=four1";
    private static final String MULTI_VALUES = "value=two,one";
    private static final String SORTED_MULTI_VALUES = "five=two,one&four=two,one&one=two,one&three=two,one&two=two,one";

    private static final int MAX_SINGLE_HEADER_SIZE = 2048;

    private static final SplitByChar charSplitter = new CustomSplitByChar();
    private static final JoinByChar charJoiner = new CustomJoinByChar();
    private static final CharSeparatedValueSorter valueSorter = new SplittingCharSeparatedValueSorter(charSplitter,charJoiner);

    private static final Map<String,Collection<String>> headers = new HashMap<String,Collection<String>>() {{
        put(CONTENT_TYPE, Arrays.asList("application/x-www-form-urlencoded"));
    }};



    private HttpServletRequest getMockHttpServletRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders(CONTENT_TYPE)).thenReturn(Collections.enumeration(headers.get(CONTENT_TYPE)));
        when(request.getHeaders(HOST)).thenReturn(null);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(CONTENT_TYPE,HOST)));
        when(request.getParameterMap()).thenReturn(Collections.singletonMap("four",new String[]{"four1"}));

        return request;
    }

    @Test
    public void testUnSortedParameterValue() throws Exception {

        ParamsAttributeExtractor extractor = new ParamsAttributeExtractor(true,false,valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        assertArrayEquals("Header should be string '"+POST_BODY+"'",POST_BODY.getBytes(),element.getElementCopy());
    }

    @Test
    public void testUnSortedParameterValues() throws Exception {

        ParamsAttributeExtractor extractor = new ParamsAttributeExtractor(true,false,valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getParameterMap()).thenReturn(new HashMap<String,String[]>(){{
            put("value",new String[]{"two","one"});
        }});


        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());


        assertArrayEquals("Header should be string '"+MULTI_VALUES+"'",MULTI_VALUES.getBytes(),element.getElementCopy());
    }

    @Test
    public void testSortedParameterValues() throws Exception {

        ParamsAttributeExtractor extractor = new ParamsAttributeExtractor(true,true,valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getParameterMap()).thenReturn(new HashMap<String,String[]>(){{
            put("two",new String[]{"two","one"});
            put("one",new String[]{"two","one"});
            put("three",new String[]{"two","one"});
            put("four",new String[]{"two","one"});
            put("five",new String[]{"two","one"});
        }});


        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());


        assertArrayEquals("Header should be string '"+SORTED_MULTI_VALUES+"'",SORTED_MULTI_VALUES.getBytes(),element.getElementCopy());
    }

    @Test
    public void testRequiredParameterValues() throws Exception {

        ParamsAttributeExtractor extractor = new ParamsAttributeExtractor(false,false,valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getParameterMap()).thenReturn(Collections.EMPTY_MAP);


        CacheKeyElement element = extractor.getAttribute(request);

        assertFalse(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty",CacheKeyElement.EMPTY_VALUE,element.getElement());


        request = getMockHttpServletRequest();
        when(request.getParameterMap()).thenReturn(null);


        element = extractor.getAttribute(request);

        assertFalse(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty",CacheKeyElement.EMPTY_VALUE,element.getElement());
    }


    @Test
    public void testOptionalButMissingParameterValues() throws Exception {

        ParamsAttributeExtractor extractor = new ParamsAttributeExtractor(true,false,valueSorter);

        HttpServletRequest request = getMockHttpServletRequest();
        when(request.getParameterMap()).thenReturn(Collections.EMPTY_MAP);


        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());

        // we test for same instance as we wnat the compiled "" empty string.
        // not a new String("")
        assertSame("Header should be empty",CacheKeyElement.EMPTY_VALUE,element.getElement());
    }
}