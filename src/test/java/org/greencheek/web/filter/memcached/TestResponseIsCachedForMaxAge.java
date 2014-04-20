package org.greencheek.web.filter.memcached;

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonFactory;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class TestResponseIsCachedForMaxAge {

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
        testMaxAge();
    }

    @Test
    public void testServlet3MaxAge() {
        server.setupServlet3Filter("localhost:"+memcached.getPort());
        testMaxAge();
    }

    private void testMaxAge() {
        String url = server.setupServlet("/maxage/*","maxageservlet","org.greencheek.web.filter.memcached.servlets.MaxAgeServlet",false);
        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        System.out.println(url);
        Request r = server.getHttpClient().prepareGet(url).build();
        try {
            Response response = server.getHttpClient().executeRequest(r).get();
            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_MISS_HEADER_VALUE,response.getHeader(PublishToMemcachedFilter.DEFAULT_CACHE_STATUS_HEADER_NAME));


            Thread.sleep(1000);
            r = server.getHttpClient().prepareGet(url).build();
            Response cachedResponse = server.getHttpClient().executeRequest(r).get();

            assertEquals(PublishToMemcachedFilter.DEFAULT_CACHE_HIT_HEADER_VALUE,cachedResponse.getHeader(PublishToMemcachedFilter.DEFAULT_CACHE_STATUS_HEADER_NAME));



        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

}
