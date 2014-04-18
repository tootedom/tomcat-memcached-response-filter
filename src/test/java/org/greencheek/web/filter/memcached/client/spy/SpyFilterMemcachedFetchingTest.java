package org.greencheek.web.filter.memcached.client.spy;

import org.greencheek.web.filter.memcached.domain.CachedResponse;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class SpyFilterMemcachedFetchingTest {

    @Test
    public void testSingleHeaderContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotEquals(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,String> content = response.getHeaders();

        assertTrue(content.containsKey("Content-Type"));

        assertEquals("text/plain",content.get("Content-Type"));
    }

    @Test
    public void testMultiHeaderContent() {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(8192,8192*2);

        buffer.append(new String("Content-Type: text/plain\r\n").getBytes());
        buffer.append(new String("Cookie: xxxx\r\n").getBytes());
        buffer.append(new String("Content-Encoding: gzip\r\n").getBytes());
        buffer.append(new String("Cache-Control: max-age=10\r\n").getBytes());
        buffer.append(new String("Content-Length: 10\r\n\r\n").getBytes());
        buffer.append(new String("hello").getBytes());

        SpyFilterMemcachedFetching fetching = new SpyFilterMemcachedFetching(null,null);

        CachedResponse response = fetching.parseCachedResponse(buffer.toByteArray());

        assertNotEquals(CachedResponse.MISS, response);

        System.out.println(response.getHeaders());

        Map<String,String> content = response.getHeaders();

        assertTrue(content.containsKey("Content-Type"));
        assertTrue(content.containsKey("Content-Length"));

        assertEquals("text/plain",content.get("Content-Type"));
        assertEquals("10",content.get("Content-Length"));
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
