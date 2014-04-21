package org.greencheek.web.filter.memcached;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.dateformatting.DateHeaderFormatter;
import org.greencheek.web.filter.memcached.dateformatting.QueueBasedDateFormatter;
import org.greencheek.web.filter.memcached.servlets.AddIntHeaderServlet;
import org.greencheek.web.filter.memcached.servlets.JSESSIONIDServlet;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonFactory;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

/**
 * Created by dominictootell on 21/04/2014.
 */
public class TestFilterConfiguration {
    private final static Pattern RESPONSE_BODY_TIME = Pattern.compile("Time:\\((\\d+)\\)");

    EmbeddedTomcatServer server;
    MemcachedDaemonWrapper memcached;

    @Before
    public void setUp() {
        server = new EmbeddedTomcatServer();
        server.setupTomcat("/filter");
        memcached = MemcachedDaemonFactory.createMemcachedDaemon(false);

        if(memcached.getDaemon()==null) {
            throw new RuntimeException("Unable to start local memcached");
        }
    }

    @After
    public void tearDown() {
        server.shutdownTomcat();
        MemcachedDaemonFactory.stopMemcachedDaemon(memcached);
    }


    @Test
    public void testIntHeaderIsAddedAndParsedWithServlet2() {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};
        server.setupServlet2Filter("localhost:"+memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/int/*","date","org.greencheek.web.filter.memcached.servlets.AddIntHeaderServlet",true);

        testIntHeader(url);
    }

    @Test
    public void testDateHeaderIsAddedAndParsedWithServlet2() {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};
        server.setupServlet2Filter("localhost:"+memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.AddDateServlet",true);

        testDateHeader(url);
    }


    @Test
    public void testDateHeaderIsAddedAndParsed() {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.AddDateServlet",true);

        testDateHeader(url);
    }

    @Test
    public void testCachingOnJSESSIONIDCookie() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,"$scheme$request_method$request_uri$cookie_jsessionid");
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.JSESSIONIDServlet",true);

        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
        System.out.println(getCookieString(response,"JSESSIONID"));
        System.out.println(response.getHeader(JSESSIONIDServlet.HEADER));

        List<Cookie> cookies = response.getCookies();
        response = executeGetRequest(url,cookies);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url,cookies);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));

        System.out.println(getCookieString(response,"JSESSIONID"));
        System.out.println(response.getHeader(JSESSIONIDServlet.HEADER));


    }



    private String getCacheHeader(Response response) {
        return response.getHeader(CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME);

    }

    private Response executeGetRequest(String url) throws Exception {
        Request r = server.getHttpClient().prepareGet(url).build();
        return server.getHttpClient().executeRequest(r).get();
    }

    private Response executeGetRequest(String url,List<Cookie> cookies) throws Exception {
        AsyncHttpClient.BoundRequestBuilder rBuilder = server.getHttpClient().prepareGet(url);
        for(Cookie c : cookies) {
            rBuilder.addCookie(c);
        }
        Request r = rBuilder.build();
        return server.getHttpClient().executeRequest(r).get();
    }


    private String getCookieString(Response response, String key) {
        for(Cookie c : response.getCookies()) {
            if(c.getName().equalsIgnoreCase(key)) {
                return c.getValue();
            }
        }
        return "";
    }

    private String getTime(Response response) {
        try {
            Matcher m = RESPONSE_BODY_TIME.matcher(response.getResponseBody());
            assertTrue(m.find());
            assertTrue(m.groupCount()>0);
            return m.group(1);
        } catch(IOException e) {
            fail("Failed to obtain response body");
            return "";
        }
    }


    private void testIntHeader(String url) {
        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        System.out.println(url);
        try {
            Response response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
            String time = getTime(response);
            assertEquals(""+AddIntHeaderServlet.VALUE,response.getHeader(AddIntHeaderServlet.HEADER));

            Thread.sleep(1000);
            response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));
            String time2 = getTime(response);
            assertEquals(time,time2);
            assertEquals(""+AddIntHeaderServlet.VALUE,response.getHeader(AddIntHeaderServlet.HEADER));

            // wait for 5 seconds (expiry is 3)
            Thread.sleep(5000);
            response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
            String time3 = getTime(response);
            assertNotEquals(time,time3);
            assertEquals(""+AddIntHeaderServlet.VALUE,response.getHeader(AddIntHeaderServlet.HEADER));




        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    private void testDateHeader(String url) {
        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        System.out.println(url);
        DateHeaderFormatter formatter = new QueueBasedDateFormatter();
        try {
            Response response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
            String time = getTime(response);
            String formattedDate = formatter.toDate(Long.parseLong(time));
            assertEquals(formattedDate,response.getHeader("X-Now"));

            Thread.sleep(1000);
            response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));
            String time2 = getTime(response);
            assertEquals(time,time2);
            formattedDate = formatter.toDate(Long.parseLong(time2));
            assertEquals(formattedDate,response.getHeader("X-Now"));

            // wait for 5 seconds (expiry is 3)
            Thread.sleep(5000);
            response = executeGetRequest(url);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
            String time3 = getTime(response);
            assertNotEquals(time, time3);
            formattedDate = formatter.toDate(Long.parseLong(time3));
            assertEquals(formattedDate, response.getHeader("X-Now"));




        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }
}
