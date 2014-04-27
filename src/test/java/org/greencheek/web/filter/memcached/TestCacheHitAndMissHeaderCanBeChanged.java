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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class TestCacheHitAndMissHeaderCanBeChanged {

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
    public void testCustomCacheHeaders() {
        final String cacheHeader = "X-Caching-Status";
        final String hitHeader = "Whammy";
        final String missHeader ="Baxter";
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHE_STATUS_HEADER_NAME, cacheHeader);
            put(PublishToMemcachedFilter.MEMCACHED_CACHE_STATUS_HIT_VALUE, hitHeader);
            put(PublishToMemcachedFilter.MEMCACHED_CACHE_STATUS_MISS_VALUE, missHeader);
        }};
        server.setupServlet3Filter("localhost:" + memcached.getPort(),null,filterInitParams);
        testCacheHeader(cacheHeader,missHeader,hitHeader);
    }


    private void testCacheHeader(String headerName, String missValue, String hitValue) {
        String url = server.setupServlet("/maxage/*","maxageservlet","org.greencheek.web.filter.memcached.servlets.MaxAgeServlet",false);
        assertTrue(server.startTomcat());
        url = server.replacePort(url) + "?maxage=3";
        System.out.println(url);
        try {
            assertEquals(missValue,getCacheHeader(url,headerName));
            Thread.sleep(1000);
            assertEquals(hitValue, getCacheHeader(url,headerName));
            assertEquals(hitValue, getCacheHeader(url,headerName));
            assertEquals(hitValue, getCacheHeader(url,headerName));
            assertEquals(hitValue, getCacheHeader(url,headerName));
            // The cache is for 3 seconds.  Wait for 5 and issue the request again.
            Thread.sleep(5000);
            assertEquals(missValue,getCacheHeader(url,headerName));

        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getCacheHeader(String url,String headerName) throws Exception {
        AsyncHttpClient.BoundRequestBuilder builder = server.getHttpClient().prepareGet(url);

        Request r = builder.build();

        Response cachedResponse = server.getHttpClient().executeRequest(r).get();
        return cachedResponse.getHeader(headerName);

    }
}
