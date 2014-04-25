package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HeaderAttributeExtractorTest {

    private static final Map<String,String> headers = new HashMap<String,String>() {{ put("Accept","text/plain");
        put("Accept-Encoding","gzip,deflate");
        put("Content-Length", "10");
        put("Content-Type","text/plain");
    }};


    @Test
    public void testOptionalHeaderWithNoValue() throws Exception {
        HeaderAttributeExtractor extractor = new HeaderAttributeExtractor("content-type",true,
                false, new CustomSplitByChar());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(Collections.enumeration(Collections.singletonList(headers.get("Accept-Encoding"))));

        CacheKeyElement element = extractor.getAttribute(request);

        assertTrue(element.isAvailable());
    }
}