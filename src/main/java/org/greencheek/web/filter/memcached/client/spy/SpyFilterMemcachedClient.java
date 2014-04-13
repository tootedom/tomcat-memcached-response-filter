package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import org.greencheek.web.filter.memcached.client.FilterMemcachedClient;
import org.greencheek.web.filter.memcached.io.ResizableByteBufferNoBoundsCheckingBackedOutputStream;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyFilterMemcachedClient implements FilterMemcachedClient {

    private final net.spy.memcached.MemcachedClient client;
    private final int headersLength;
    private static final byte[] newLine = new byte[]{(byte)'\r',(byte)'\n'};
    private static final byte[] headerNameSeparator = new byte[]{':',' '};


    public SpyFilterMemcachedClient(MemcachedClient client) {
        this(client,8192);
    }

    public SpyFilterMemcachedClient(MemcachedClient client,int headersLength) {
        this.headersLength = headersLength;
        this.client = client;
    }

    @Override
    public void writeToCached(String key, int expiryInSeconds, Set<String> additionalContent,
                              Map<String, Collection<String>> responseHeaders, ResizeableByteBuffer content) {
        int contentLength = content.size();
        ResizeableByteBuffer memcachedContent = new ResizeableByteBuffer(contentLength,contentLength + headersLength);

        for(String string : additionalContent) {
            addStringToContent(memcachedContent,string);
            memcachedContent.append(newLine);
        }


        for(Map.Entry<String,Collection<String>> entry : responseHeaders.entrySet()) {
            byte[] headerName = getBytes(entry.getKey());
            for(String value : entry.getValue()) {
                memcachedContent.append(headerName);
                memcachedContent.append(headerNameSeparator);
                addStringToContent(memcachedContent, value);
                memcachedContent.append(newLine);
            }
        }

        memcachedContent.append(newLine);
        memcachedContent.append(newLine);
        memcachedContent.append(content.getBuf(),0,contentLength);

        if(content.canWrite()) {
            writeToMemcached(key, expiryInSeconds, memcachedContent.toByteArray());
        }
    }

    private void addStringToContent(ResizeableByteBuffer content, String value) {
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
