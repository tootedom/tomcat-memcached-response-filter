package org.greencheek.web.filter.memcached.cachekey;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class DefaultCacheKeyCreatorTest {

    private CacheKeyCreator cacheKeyCreator;


    @Test
    public void testPathAndQueryStringReturnByRequestUri() throws Exception {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        String expected = path+queryString;
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$request_uri");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testPathReturnedByPathSpec() {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        String expected = path;
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$uri");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testMethodReturnedByMethodSpec() {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        String expected = "PUT";
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$request_method");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getMethod()).thenReturn("PUT");

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testSchemeReturnedBySchemeSpec() {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        String expected = "https";
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$scheme");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getScheme()).thenReturn("https");

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testQueryReturnedByQuerySpec() {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        String expected = queryString;
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$args");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testHeaderReturnedByHeaderSpec() throws Exception {
        Map<String,String> headers = new HashMap<String,String>() {{ put("Content-Type","text/plain");
            put("Accept-Encoding","gzip,deflate");}};

        String expected = "text/plain";
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$header_Content-Type");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(new HashSet<String>(){{ add("Content-Type");add("Content-Length");}}) );
        when(request.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));
        when(request.getHeader("Accept-Encoding")).thenReturn(headers.get("Accept-Encoding"));

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
    }

    @Test
    public void testTwoHeadersReturnedByHeaderSpec() throws Exception {
        Map<String,String> headers = new HashMap<String,String>() {{ put("Content-Type","text/plain");
            put("Accept-Encoding","gzip,deflate");
            put("Content-Length", "10");
        }};

        String expected = "text/plain10";
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$header_Content-Type$header_Content-Length");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(new HashSet<String>(){{ add("Content-Type");add("Content-Length");}}) );
        when(request.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));
        when(request.getHeader("Content-Length")).thenReturn(headers.get("Content-Length"));
        when(request.getHeader("Accept-Encoding")).thenReturn(headers.get("Accept-Encoding"));

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(new HashSet<String>(){{ add("Content-Type");add("Content-Length");}}) );
        when(request.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));
        when(request.getHeader("Content-Length")).thenReturn(headers.get("Content-Length"));
        when(request.getHeader("Accept-Encoding")).thenReturn(headers.get("Accept-Encoding"));

        expected = "10text/plain";
        this.cacheKeyCreator = new DefaultCacheKeyCreator("$header_Content-Length$header_Content-Type");
        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());

    }

    @Test
    public void testCookieReturnedWhenCookieSpecUsed() throws Exception {
        Map<String,String> headers = new HashMap<String,String>() {{ put("Content-Type","text/plain");
            put("Accept-Encoding","gzip,deflate");
            put("Content-Length", "10");
        }};

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(createCookies());
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(new HashSet<String>(){{ add("Content-Type");
            add("Content-Length");
            add("Accept-Encoding");
        }}) );
        when(request.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));
        when(request.getHeader("Content-Length")).thenReturn(headers.get("Content-Length"));
        when(request.getHeader("Accept-Encoding")).thenReturn(headers.get("Accept-Encoding"));

        this.cacheKeyCreator = new DefaultCacheKeyCreator("$header_Content-Type$cookie_cookie1$header_Content-Length$cookie_CooKIE3");

        String expected = "text/plaincookie1=value1; Domain=www.test1.com; Max-Age=10; Path=/path110cookie3=value3; Version=1; Domain=www.test3.com; Max-Age=55; Path=/path3; Secure; HttpOnly";

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());


    }

    @Test
    public void testCombinationOfKeySpecs() {
        String path = "/path/value";
        String queryString = "bob=xxx&fred=yyyy";
        Map<String,String> headers = new HashMap<String,String>() {{ put("Accept","text/plain");
            put("Accept-Encoding","gzip,deflate");
            put("Content-Length", "10");
        }};

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(createCookies());
        when(request.getHeaderNames()).thenReturn(java.util.Collections.enumeration(new HashSet<String>(){{ add("Accept");
            add("Content-Length");
            add("Accept-Encoding");
        }}) );
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString);

        when(request.getMethod()).thenReturn("GET");
        when(request.getScheme()).thenReturn("https");
        when(request.getHeader("Accept")).thenReturn(headers.get("Accept"));
        when(request.getHeader("Content-Length")).thenReturn(headers.get("Content-Length"));
        when(request.getHeader("Accept-Encoding")).thenReturn(headers.get("Accept-Encoding"));


        this.cacheKeyCreator = new DefaultCacheKeyCreator("$request_method$scheme$request_uri$header_accept-encoding$header_accept$cookie_cookie2");

        String expected = "GEThttps/path/valuebob=xxx&fred=yyyygzip,deflatetext/plaincookie2=value2; Version=1; Comment=blahblah; Domain=www.test2.com; Max-Age=167; Path=/path2";

        assertEquals("Cache key should be:" + expected, expected, cacheKeyCreator.createCacheKey(request).getKey());


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
