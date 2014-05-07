package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CookieAttributeExtractorTest {

    HttpServletRequest request;
    HttpServletRequest requestNoCookies;

    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(createCookies());

        requestNoCookies = mock(HttpServletRequest.class);
        when(requestNoCookies.getCookies()).thenReturn(null);

    }



    @Test
    public void testAvailableCookie() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie1",true);
        CacheKeyElement element = extractor.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("cookie1=value1; Domain=www.test1.com; Max-Age=10; Path=/path1".getBytes(), element.getElement());
    }

    @Test
    public void testAvailableCookieVersion1WithComment() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie2",true);
        CacheKeyElement element = extractor.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("cookie2=value2; Version=1; Comment=blahblah; Domain=www.test2.com; Max-Age=167; Path=/path2".getBytes(), element.getElement());
    }

    @Test
    public void testAvailableCookieSecureAndHttpOnly() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie3",true);
        CacheKeyElement element = extractor.getAttribute(request);
        assertTrue(element.isAvailable());
        assertArrayEquals("cookie3=value3; Version=1; Domain=www.test3.com; Max-Age=55; Path=/path3; Secure; HttpOnly".getBytes(), element.getElement());
    }


    @Test
    public void testNonExistentCookieAttribute() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie4",true);
        CacheKeyElement element = extractor.getAttribute(request);
        assertFalse(element.isAvailable());
        assertSame(CacheKeyElement.EMPTY_VALUE,element.getElement());
    }

    @Test
    public void testNoCookiesInRequest() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie4",false);
        CacheKeyElement element = extractor.getAttribute(requestNoCookies);
        assertFalse(element.isAvailable());
        assertSame(CacheKeyElement.EMPTY_VALUE,element.getElement());
    }

    @Test
    public void testNoCookiesInRequestWithOptionalExtractor() throws Exception {
        CookieAttributeExtractor extractor = new CookieAttributeExtractor("cookie4",true);
        CacheKeyElement element = extractor.getAttribute(requestNoCookies);
        assertTrue(element.isAvailable());
        assertSame(CacheKeyElement.EMPTY_VALUE,element.getElement());
    }

    private Cookie createCookies(String name,
                                 String value,
                                 String path, String domain, int age,
                                 boolean secure, int version, String comment, boolean httpOnly) {
        Cookie c = new Cookie(name,value);
        if(path!=null) c.setPath(path);
        if(version>0) c.setVersion(version);
        if(domain!=null) c.setDomain(domain);
        if(secure) c.setSecure(true);
        else c.setSecure(false);
        c.setMaxAge(age);
        if(comment!=null) c.setComment(comment);
        c.setHttpOnly(httpOnly);

        return c;
    }

    private Cookie[] createCookies() {
        return new Cookie[] {
                createCookies("cookie1","value1","/path1","www.test1.com",10,false,0,null,false),
                createCookies("cookie2","value2","/path2","www.test2.com",167,false,1,"blahblah",false),
                createCookies("cookie3","value3","/path3","www.test3.com",55,true,1,null,true),
        };
    }
}