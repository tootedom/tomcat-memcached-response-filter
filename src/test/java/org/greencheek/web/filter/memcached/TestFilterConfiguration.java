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
import java.util.Collections;
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
    private final static Pattern RESPONSE_BODY_SESSION_ID = Pattern.compile("Session:\\((\\w+)\\)");

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
    public void testForceCaching() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitParamsForced = new HashMap<String,String>(3,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
            put(PublishToMemcachedFilter.MEMCACHED_FORCE_CACHE,"true");
            put(PublishToMemcachedFilter.MEMCACHED_FORCE_EXPIRY,"3");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"/nocache",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocachedforced","/nocacheforced",filterInitParamsForced);
        String url = server.setupServlet("/nocache/*","nocache","org.greencheek.web.filter.memcached.servlets.NoCacheServlet",true);
        String urlforced = server.setupServlet("/nocacheforced/*","nocacheforce","org.greencheek.web.filter.memcached.servlets.NoCacheServlet",true);
        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        urlforced = server.replacePort(urlforced);
        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));

        response = executeGetRequest(urlforced);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(urlforced);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
    }

    @Test
    public void testPrivateCaching() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitParamsForced = new HashMap<String,String>(3,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
            put(PublishToMemcachedFilter.MEMCACHED_CACHE_PRIVATE,"true");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"/noprivate",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"private","/private",filterInitParamsForced);
        String url = server.setupServlet("/noprivate/*","noprivate","org.greencheek.web.filter.memcached.servlets.CacheControlWithConfigurablePrivateServlet",true);
        String urlforced = server.setupServlet("/private/*","private","org.greencheek.web.filter.memcached.servlets.CacheControlWithConfigurablePrivateServlet",true);
        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        url = url+"?private=1";
        urlforced = server.replacePort(urlforced);
        urlforced = urlforced+"?private=1";
        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));

        response = executeGetRequest(urlforced);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(urlforced);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
    }


    @Test
    public void testCachingWithoutCacheControl() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitParamsForced = new HashMap<String,String>(2,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
            put(PublishToMemcachedFilter.MEMCACHED_CACHE_WITH_NO_CACHE_CONTROL,"false");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"/nocachecontrol",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocachedcontrolforced","/nocachedcontrolforced",filterInitParamsForced);
        String url = server.setupServlet("/nocachecontrol/*","nocache","org.greencheek.web.filter.memcached.servlets.NoCacheControlServlet",true);
        String urlforced = server.setupServlet("/nocachedcontrolforced/*","nocachedcontrolforced","org.greencheek.web.filter.memcached.servlets.NoCacheControlServlet",true);
        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        urlforced = server.replacePort(urlforced);
        Response response = executeGetRequest(url);
        String time = getTime(response);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
        assertEquals(time,getTime(response));

        response = executeGetRequest(urlforced);
        time = getTime(response);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executeGetRequest(urlforced);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        assertNotEquals(time,getTime(response));
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
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,"$scheme$request_method$uri$args?$cookie_jsessionid");
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY,"10");
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.JSESSIONIDServlet",true);

        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));

        List<Cookie> cookies1 = response.getCookies();
        response = executeGetRequest(url,cookies1);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url,cookies1);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));


        List<Cookie> cookies2 = response.getCookies();
        // This request will cache.
        executeGetRequest(url,cookies2);

        // the request will get from the cache
        response = executeGetRequest(url,cookies2);
        String sessionId2 = getSessionID(response);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url,cookies1);

        String sessionId1 = getSessionID(response);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));

        assertNotEquals("The two session ids must be different",sessionId1,sessionId2);

    }

    @Test
    public void testOptionalCachingOnJSESSIONIDCookie() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,"$scheme$request_method$uri$args?$cookie_jsessionid?");
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY,"10");
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.JSESSIONIDServlet",true);

        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));

        List<Cookie> cookies = response.getCookies();
        String sessionId = getSessionID(response);

        response = executeGetRequest(url,cookies);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url,cookies);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));

        response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE,getCacheHeader(response));
        assertEquals(sessionId, getSessionID(response));

        Cookie c = cookies.get(0);
        Cookie madeupJsessionId = new Cookie("JSESSIONID","xx","xx",c.getDomain(),c.getPath(),
                c.getExpires(),c.getMaxAge(),c.isSecure(),c.isHttpOnly());

        response = executeGetRequest(url, Collections.singletonList(madeupJsessionId));
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
        assertNotEquals(sessionId, getSessionID(response));

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

    /**
     * Returns the time from the html body
     * @param response
     * @return
     */
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

    /**
     * Returns the session id from the html body
     * @param response
     * @return
     */
    private String getSessionID(Response response) {
        try {
            Matcher m = RESPONSE_BODY_SESSION_ID.matcher(response.getResponseBody());
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
//            System.out.println(response.getResponseBody());

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
