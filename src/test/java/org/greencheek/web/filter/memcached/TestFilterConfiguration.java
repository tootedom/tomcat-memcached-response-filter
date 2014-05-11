package org.greencheek.web.filter.memcached;

import com.excilys.ebi.gatling.http.request.builder.PostHttpRequestBuilder;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.StringPart;
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
    private final static Pattern RESPONSE_BODY_CONTENT = Pattern.compile("content:\\(([^)]+)\\)");

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
    public void testSettingGetTimeout() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_GET_TIMEOUT, "1000");
            put(PublishToMemcachedFilter.MEMCACHED_NODE_FAILURE_MODE,"retry");
        }};
        server.setupServlet2Filter("localhost:"+memcached.getPort(),null,filterInitParams);
        String url = server.setupServlet("/date/*","date","org.greencheek.web.filter.memcached.servlets.AddDateServlet",true);

        assertTrue(server.startTomcat());
        url = server.replacePort(url);
        System.out.println(url);
        DateHeaderFormatter formatter = new QueueBasedDateFormatter();

        Response response = executeGetRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
        String time = getTime(response);
        String formattedDate = formatter.toDate(Long.parseLong(time));
        assertEquals(formattedDate,response.getHeader("X-Now"));

        memcached.getDaemon().stop();
        Thread.sleep(500);

        long start = System.currentTimeMillis();
        response = executeGetRequest(url);
        long taken = System.currentTimeMillis() - start;
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE,getCacheHeader(response));
        time = getTime(response);
        formattedDate = formatter.toDate(Long.parseLong(time));
        assertEquals(formattedDate,response.getHeader("X-Now"));
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
    public void testPostCaching() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitPostAllowedParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS, "get,post");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocaching","/posting/not/*",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"caching","/posting/is/*",filterInitPostAllowedParams);

        String url = server.setupServlet("/posting/not/cacheable/*","posting","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);
        String urlCachable = server.setupServlet("/posting/is/cacheable/*","postingcachable","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);


        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        urlCachable = server.replacePort(urlCachable);

        Response response = executePostRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(url);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));


        response = executePostRequest(urlCachable);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable);
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

    }

    @Test
    public void testLargePostCaching() throws Exception {
        Map<String,String> filterInitPostAllowedParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS, "get,post");
            put(PublishToMemcachedFilter.MEMCACHED_MAX_POST_BODY_SIZE,"5526");
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,CacheConfigGlobals.DEFAULT_CACHE_KEY+"$body");
        }};

        String content = "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";

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"caching","/posting/is/*",filterInitPostAllowedParams);

        String urlCachable = server.setupServlet("/posting/is/cacheable/*","postingcachable","org.greencheek.web.filter.memcached.servlets.PutPostServlet",true);


        assertTrue(server.startTomcat());

        urlCachable = server.replacePort(urlCachable);


        Response response = executePostRequest(urlCachable,content);
        String content1 = getContent(response);

        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,content);
        String content2 = getContent(response);

        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

        assertEquals("Returned content should be equal to post body",content,content1);
        assertEquals("Returned content should be equal to post body",content,content2);

        assertEquals("Returned content should be equal to post body",content1,content2);
    }

    @Test
    public void testNoPostCachingForLargePost() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitPostAllowedParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS, "get,post");
            put(PublishToMemcachedFilter.MEMCACHED_MAX_POST_BODY_SIZE,"10");
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,CacheConfigGlobals.DEFAULT_CACHE_KEY+"$body");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocaching","/posting/not/*",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"caching","/posting/is/*",filterInitPostAllowedParams);

        String url = server.setupServlet("/posting/not/cacheable/*","posting","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);
        String urlCachable = server.setupServlet("/posting/is/cacheable/*","postingcachable","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);


        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        urlCachable = server.replacePort(urlCachable);

        Response response = executePostRequest(url,"date=hello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(url,"date=hello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));


        response = executePostRequest(urlCachable,"date=hellothisismorethattheallowed");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"date=hellothisismorethattheallowed");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));

        response = executePostRequest(urlCachable,"hellohello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"hellohello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

        response = executePostRequest(urlCachable,"hellohello1");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"hellohello1");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
    }

    @Test
    public void testPostCachingForParams() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitPostAllowedParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS, "get,post");
            put(PublishToMemcachedFilter.MEMCACHED_MAX_POST_BODY_SIZE,"10");
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,CacheConfigGlobals.DEFAULT_CACHE_KEY+"$params");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocaching","/posting/not/*",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"caching","/posting/is/*",filterInitPostAllowedParams);

        String url = server.setupServlet("/posting/not/cacheable/*","posting","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);
        String urlCachable = server.setupServlet("/posting/is/cacheable/*","postingcachable","org.greencheek.web.filter.memcached.servlets.ServiceServlet",true);


        assertTrue(server.startTomcat());

        url = server.replacePort(url)+"?queryParam=1";
        urlCachable = server.replacePort(urlCachable)+"?queryParam=1";

        Response response = executePostRequest(urlCachable,"date=hello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"date=hello2");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));


        response = executePostRequest(urlCachable,"date=hello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"date=hello2");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

        response = executePostRequest(urlCachable,"message=hellohello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"message=hellohello");
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));

    }


    @Test
    public void testPostKeyWithBodyCaching() throws Exception {
        Map<String,String> filterInitParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_EXPIRY, "3");
        }};

        Map<String,String> filterInitPostAllowedParams = new HashMap<String,String>(1,1.0f) {{
            put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS, "get,post");
            put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,CacheConfigGlobals.DEFAULT_CACHE_KEY+"$body");
        }};

        server.setupServlet3Filter("localhost:" + memcached.getPort(),"nocaching","/posting/not/*",filterInitParams);
        server.setupServlet3Filter("localhost:" + memcached.getPort(),"caching","/posting/is/*",filterInitPostAllowedParams);

        String url = server.setupServlet("/posting/not/cacheable/*","posting","org.greencheek.web.filter.memcached.servlets.PutPostServlet",true);
        String urlCachable = server.setupServlet("/posting/is/cacheable/*","postingcachable","org.greencheek.web.filter.memcached.servlets.PutPostServlet",true);


        assertTrue(server.startTomcat());

        url = server.replacePort(url);
        urlCachable = server.replacePort(urlCachable);

        Response response = executePostRequest(url,"date=hello");
        assertEquals("Posted content is not as expected","date=hello",getContent(response));
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(url,"date=hello");
        assertEquals("Posted content is not as expected","date=hello",getContent(response));
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));


        response = executePostRequest(urlCachable,"date=hello2");
        assertEquals("Posted content is not as expected","date=hello2",getContent(response));
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_MISS_HEADER_VALUE, getCacheHeader(response));
        response = executePostRequest(urlCachable,"date=hello2");
        assertEquals("Posted content is not as expected","date=hello2",getContent(response));
        assertEquals(CacheConfigGlobals.DEFAULT_CACHE_HIT_HEADER_VALUE, getCacheHeader(response));
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

    private Response executePostRequest(String url) throws Exception {
        Request r = server.getHttpClient().preparePost(url).build();
        return server.getHttpClient().executeRequest(r).get();
    }

    private Response executePostRequest(String url, String body) throws Exception {
        AsyncHttpClient.BoundRequestBuilder builder = server.getHttpClient().preparePost(url);
        builder.setBody(body);
        builder.setBodyEncoding("UTF-8");
        builder.addHeader("Content-Type","application/x-www-form-urlencoded");
        Request r = builder.build();
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
     * Returns the time from the html body
     * @param response
     * @return
     */
    private String getContent(Response response) {
        try {
            Matcher m = RESPONSE_BODY_CONTENT.matcher(response.getResponseBody());
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
