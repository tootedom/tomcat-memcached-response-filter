package org.greencheek.web.filter.memcached;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonFactory;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class TestResponseIsNotCachedForContentLargerThanBuffer {

    private final static Pattern RESPONSE_BODY_TIME = Pattern.compile("OUTPUTSTREAM:\\((\\d+)\\)");

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
    public void testContentLargerThanSpecifiedInInitIsNotCached() {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_RESPONSE_BODY_SIZE, ""+8192*2);
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        testNotCachedContent();
    }

    @Test
    public void testSmallerContentThanSpecifiedInInitIsCached() {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_RESPONSE_BODY_SIZE, ""+8192*5);
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        testCachedContent();
    }


    private String getHeaderTime(Response response) {
        return response.getHeader("X-LastTime");
    }

    private void testNotCachedContent() {
        String url = server.setupServlet("/maxage/*","maxageservlet","org.greencheek.web.filter.memcached.servlets.LargeContentServlet",false);
        assertTrue(server.startTomcat());
        url = server.replacePort(url) + "?maxage=3";
        System.out.println(url);
        try {
            Response response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            String time = getHeaderTime(response);

            Thread.sleep(1000);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));

            // The cache is for 3 seconds.  Wait for 5 and issue the request again.
            Thread.sleep(5000);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));


        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    private void testCachedContent() {
        String url = server.setupServlet("/maxage/*","maxageservlet","org.greencheek.web.filter.memcached.servlets.LargeContentServlet",false);
        assertTrue(server.startTomcat());
        url = server.replacePort(url) + "?maxage=3";
        System.out.println(url);
        try {
            Response response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            String time = getHeaderTime(response);

            Thread.sleep(1000);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
            assertEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
            assertEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
            assertEquals(time,getHeaderTime(response));

            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
            assertEquals(time,getHeaderTime(response));

            // The cache is for 3 seconds.  Wait for 5 and issue the request again.
            Thread.sleep(5000);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(time,getHeaderTime(response));


        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getCacheHeader(Response cachedResponse) throws Exception {
        return cachedResponse.getHeader(CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME);

    }

    private Response getResponse(String url,boolean nocache) throws Exception {
        AsyncHttpClient.BoundRequestBuilder builder = server.getHttpClient().prepareGet(url);
        if(nocache) {
            builder.addHeader("Cache-Control","no-cache");
        }
        Request r = builder.build();

        return server.getHttpClient().executeRequest(r).get();
    }
}
