package org.greencheek.web.filter.memcached;

import com.ning.http.client.ListenableFuture;
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
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class TestHystrixFilterConfiguration {

    EmbeddedTomcatServer server;
    MemcachedDaemonWrapper memcached;
    Executor executor;
    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(10);
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
        if(executor instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor)executor).shutdownNow();
        }

    }


    @Test
    public void testMultiRequestAreCollapsedWithDefaults() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        testMultiRequestsAreCollapsed(filterInitParams);
    }

    @Test
    public void testMultiRequestAreCollapsedWithThreadPools() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIX_CACHE_LOOKUP_EXECUTION_TYPE, "threadpool");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_SIZE, "10");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_QUEUESIZE,"2");
        }};

        testMultiRequestsAreCollapsed(filterInitParams);
    }

    @Test
    public void testMultiRequestAreCollapsedWithThreadPoolSmallBatchSize() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIX_CACHE_LOOKUP_EXECUTION_TYPE, "threadpool");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_SIZE, "10");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIC_CACHE_LOOKUP_THREAD_POOL_QUEUESIZE,"2");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIX_CACHE_LOOKUP_BATCHING_MAX_SIZE,"2");
        }};

        testMultiRequestsAreCollapsed(filterInitParams);
    }

    @Test
    public void testMultiRequestAreCollapsedWithSmallSemaphoreSize() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIX_CACHE_LOOKUP_EXECUTION_TYPE, "semaphore");
            put(HystrixPublishToMemcachedFilter.MEMCACHED_HYSTRIX_CACHE_LOOKUP_SEMAPHORE_SIZE,"3");
        }};

        testMultiRequestsAreCollapsed(filterInitParams);
    }

    public void testMultiRequestsAreCollapsed(Map<String,String> filterParams) throws Exception {

        server.setupServletHystrixFilter("localhost:"+memcached.getPort(),null,filterParams);
        String url = server.setupServlet("/int/*","date","org.greencheek.web.filter.memcached.servlets.AddIntHeaderServlet",true);
        assertTrue(server.startTomcat());
        url = server.replacePort(url);


        // Test No Sorting
        ListenableFuture<Response> response1 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response2 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response3 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response4 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response5 = executeAsyncGetRequest(url);

        CountDownLatch latch = new CountDownLatch(5);
        attachListener(latch,response1,response2,response3,response4,response5);

        boolean ok=false;
        try {
            ok = latch.await(2000, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            ok =false;
        }

        assertTrue("Should have less than 4 hits on the memcached", memcached.getDaemon().getCache().getGetCmds()<4);
        assertTrue("All Request should have compeleted",ok);

        ListenableFuture<Response> response6 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response7 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response8 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response9 = executeAsyncGetRequest(url);
        ListenableFuture<Response> response10 = executeAsyncGetRequest(url);

        latch = new CountDownLatch(5);
        attachListener(latch,response6,response7,response8,response9,response10);

        ok=false;
        try {
            ok = latch.await(2000, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            ok =false;
        }

        assertCacheHits(response6,response7,response8,response9,response10);



        assertTrue("Should have less than 7 hits on the memcached", memcached.getDaemon().getCache().getGetCmds()<7);


    }


    private void assertCacheHits(ListenableFuture<Response>... responses) throws Exception{
        int expected = responses.length;
        int count = 0;
        for(ListenableFuture<Response> res : responses) {
            if(getCacheHeader(res.get()).equalsIgnoreCase(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE)) {
                count++;
            }
        }

        assertEquals("Should have " + expected + " cache hits",expected,count);

    }


    private String getCacheHeader(Response response) {
        return response.getHeader(CacheConfigGlobals.DEFAULT_CACHE_STATUS_HEADER_NAME);

    }

    private void attachListener(final CountDownLatch latch, ListenableFuture<Response>... responses) {

        for(ListenableFuture<Response> res : responses) {
            res.addListener(new Runnable() {
                @Override
                public void run() {
                    latch.countDown();
                }
            },executor);
        }

    }

    private ListenableFuture<Response> executeAsyncGetRequest(String url) throws Exception {
        Request r = server.getHttpClient().prepareGet(url).build();
        return server.getHttpClient().executeRequest(r);
    }
}
