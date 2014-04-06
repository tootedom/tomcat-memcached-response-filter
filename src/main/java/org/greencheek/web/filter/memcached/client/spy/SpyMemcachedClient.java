package org.greencheek.web.filter.memcached.client.spy;

import org.greencheek.web.filter.memcached.client.MemcachedClient;
import org.greencheek.web.filter.memcached.io.ResizableByteBufferNoBoundsCheckingBackedOutputStream;
import org.greencheek.web.filter.memcached.io.util.CharToByteArray;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyMemcachedClient implements MemcachedClient {

    private final net.spy.memcached.MemcachedClient client;
    private final int headersLength = 8192;
    private static final byte[] newLine = new byte[]{(byte)'\r',(byte)'\n'};


    public SpyMemcachedClient(net.spy.memcached.MemcachedClient client) {
        this.client = client;
    }

    @Override
    public void writeToCached(String key, int expiryInSeconds, Set<String> additionalContent,
                              Map<String, Collection<String>> responseHeaders, byte[] content) {
        ResizableByteBufferNoBoundsCheckingBackedOutputStream memcachedContent = null;
        memcachedContent = new ResizableByteBufferNoBoundsCheckingBackedOutputStream(content.length,(int)content.length + headersLength);

        for(String string : additionalContent) {
            addStringToContent(memcachedContent,string);
            memcachedContent.append(newLine);
        }


        for(Map.Entry<String,Collection<String>> entry : responseHeaders.entrySet()) {
            byte[] headerName = getBytes(entry.getKey());
            for(String value : entry.getValue()) {
                memcachedContent.append(headerName);
                memcachedContent.append((byte) 58);
                memcachedContent.append((byte) 32);
                addStringToContent(memcachedContent, value);
                memcachedContent.append(newLine);
            }
        }

        memcachedContent.append(newLine);
        memcachedContent.append(newLine);
        memcachedContent.append(content);

        writeToMemcached(key,expiryInSeconds,memcachedContent.toByteArray());
    }

    private void addStringToContent(ResizableByteBufferNoBoundsCheckingBackedOutputStream content, String value) {
        content.append(getBytes(value));
    }

    private byte[] getBytes(String content) {
        try {
            return content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return content.getBytes();
        }
    }

    private void writeToMemcached(String key, int expiryInSeconds, byte[] content) {
        try {
            client.set(key, expiryInSeconds, content);
        } catch (Exception e) {
            // need a logger
        }
    }
}
