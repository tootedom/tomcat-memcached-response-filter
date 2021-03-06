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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class TestResponseIsCachedForOutputStream {

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
    public void testServlet2MaxAge() {
        server.setupServlet2Filter("localhost:"+memcached.getPort());
        testCachedContent();
    }


    private String getTimeFromResponseBody(Response response) {
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

    private void testCachedContent() {
        String url = server.setupServlet("/maxage/*","maxageservlet","org.greencheek.web.filter.memcached.servlets.OutputStreamServlet",false);
        assertTrue(server.startTomcat());
        url = server.replacePort(url) + "?maxage=3";
        System.out.println(url);
        try {
            Response response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            Thread.sleep(1000);
            String firstTime = getTimeFromResponseBody(response);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

            response = getResponse(url,false);
            assertEquals(firstTime,getTimeFromResponseBody(response));

            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

            response = getResponse(url,false);
            assertEquals(firstTime,getTimeFromResponseBody(response));

            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

            response = getResponse(url,false);
            assertEquals(firstTime,getTimeFromResponseBody(response));
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

            assertEquals(firstTime,getTimeFromResponseBody(response));
            // The cache is for 3 seconds.  Wait for 5 and issue the request again.
            Thread.sleep(5000);
            response = getResponse(url,false);
            assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
            assertNotEquals(firstTime,getTimeFromResponseBody(response));


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
