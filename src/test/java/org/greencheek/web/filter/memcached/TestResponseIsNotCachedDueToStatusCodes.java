package org.greencheek.web.filter.memcached;

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonFactory;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class TestResponseIsNotCachedDueToStatusCodes {

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
    public void testServlet2500NotCached() {
        server.setupServlet2Filter("localhost:" + memcached.getPort());
        testNoCachingOccurs("500");
    }

    @Test
    public void testServlet2404NotCached() {
        server.setupServlet2Filter("localhost:" + memcached.getPort());
        testNoCachingOccurs("404");
    }

    @Test
    public void testServlet3500NotCached() {
        server.setupServlet3Filter("localhost:" + memcached.getPort());
        testNoCachingOccurs("500");
    }

    @Test
    public void testServlet3404NotCached() {
        server.setupServlet3Filter("localhost:" + memcached.getPort());
        testNoCachingOccurs("404");
    }

    private void testNoCachingOccurs(String statusCode) {
        String url;
        if(statusCode.equals("500")) {
            url = server.setupServlet("/statuscode/*", "statuscode", "org.greencheek.web.filter.memcached.servlets.StatusErrorCode500Servlet", false);
        } else {
            url = server.setupServlet("/statuscode/*", "statuscode", "org.greencheek.web.filter.memcached.servlets.StatusCode404Servlet", false);

        }
        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        try {
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(url));
            Thread.sleep(1000);
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(url));
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(url));
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(url));
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(url));
            // The cache is for 3 seconds.  Wait for 5 and issue the request again.
            Thread.sleep(5000);
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(url));


        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getCacheHeader(String url) throws Exception {
        Request r = server.getHttpClient().prepareGet(url).build();
        Response cachedResponse = server.getHttpClient().executeRequest(r).get();
        String header = cachedResponse.getHeader(PublishToMemcachedFilter.DEFAULT_CACHE_STATUS_HEADER_NAME);
        return header;

    }
}
