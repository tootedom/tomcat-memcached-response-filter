package org.greencheek.web.filter.memcached.client.spy;

import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class SpyFilterMemcachedFetchingTest {

    @Test
    public void testStatusLine() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("HTTP/1.0 200 OK\r\n").getBytes());
        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotEquals(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,Collection<String>> content = response.getHeaders();

        assertTrue(content.containsKey("Content-Type"));

        assertTrue(content.get("Content-Type").contains("text/plain"));

        assertEquals(200, response.getStatusCode());


        buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("HTTP/1.0 404 Not Found\r\n").getBytes());
        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("hello").getBytes());


        response = fetching.parseCachedResponse(buffer.toByteArray());
        assertEquals(404,response.getStatusCode());
    }

    @Test
    public void testSingleHeaderContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("HTTP/1.1 503 Service Unavailable\r\n").getBytes());
        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotSame(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,Collection<String>> content = response.getHeaders();

        assertTrue(content.containsKey("Content-Type"));

        assertTrue(content.get("Content-Type").contains("text/plain"));
    }

    @Test
    public void testMultiHeaderContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("HTTP/1.0 504 Gateway Timeout\r\n").getBytes());
        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("Cookie: xxxx\r\n").getBytes());
        buffer.append(new String("Content-Encoding: gzip\r\n").getBytes());
        buffer.append(new String("Cache-Control: max-age=10\r\n").getBytes());
        buffer.append(new String("Content-Length: 10\r\n\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotSame(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,Collection<String>> content = response.getHeaders();

        assertEquals(5,content.size());

        assertTrue(content.containsKey("Content-Type"));
        assertTrue(content.containsKey("Content-Length"));

        assertTrue(content.get("Content-Type").contains("text/plain"));
        assertTrue(content.get("Content-Length").contains("10"));
    }

    @Test
    public void testMultiHeaderWithMultiValuesAndContent() throws UnsupportedEncodingException {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("HTTP/1.0 504 Gateway Timeout\r\n").getBytes());
        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("Cookie: xxxx\r\n").getBytes());
        buffer.append(new String("Content-Encoding: gzip\r\n").getBytes());
        buffer.append(new String("Cache-Control: max-age=10\r\n").getBytes());
        buffer.append(new String("Cache-Control: must-revalidate\r\n").getBytes());
        buffer.append(new String("Content-Length: 10\r\n\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotSame(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,Collection<String>> content = response.getHeaders();

        assertEquals(5,content.size());

        assertTrue(content.containsKey("Content-Type"));
        assertTrue(content.containsKey("Content-Length"));

        assertTrue(content.get("Content-Type").contains("text/plain"));
        assertTrue(content.get("Content-Length").contains("10"));

        assertEquals(2, content.get("Cache-Control").size());
        assertTrue(content.get("Cache-Control").contains("must-revalidate"));
        assertTrue(content.get("Cache-Control").contains("max-age=10"));

        assertEquals("max-age=10", content.get("Cache-Control").toArray()[0]);
        assertEquals("must-revalidate",content.get("Cache-Control").toArray()[1]);

        int contentLength = response.getContent().length - response.getContentOffset();

        assertArrayEquals("hello".getBytes("UTF-8"), new String(response.getContent(),response.getContentOffset(),contentLength,"UTF-8").getBytes("UTF-8"));
    }

    @Test
    public void testNoSingleHeaderContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("hello").getBytes());


        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        System.out.println(response.getHeaders());

        assertEquals(CachedResponse.MISS, response);

    }

    @Test
    public void testNoContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertEquals(CachedResponse.MISS, response);
    }
}
