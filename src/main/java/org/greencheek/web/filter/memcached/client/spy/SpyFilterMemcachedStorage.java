package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import org.greencheek.web.filter.memcached.cachekey.CacheKeyCreator;
import org.greencheek.web.filter.memcached.client.FilterMemcachedStorage;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyFilterMemcachedStorage implements FilterMemcachedStorage {

    private static int DEFAULT_HEADERS_LENGTH = 8192;
    private static Set<String> DEFAULT_ADDITIONAL_HEADERS = Collections.EMPTY_SET;
    private final int DEFAULT_EXPIRY_IN_SECONDS = 300;
    private static final byte[] newLine = new byte[]{(byte)'\r',(byte)'\n'};
    private static final byte[] headerNameSeparator = new byte[]{':',' '};

    private static final Set<String> DEFAULT_RESPONSE_HEADERS_TO_IGNORE;
    static {
        Set<String> headers = new HashSet<String>(9);
        headers.add("connection");
        headers.add("keep-alive");
        headers.add("proxy-authenticate");
        headers.add("proxy-authorization");
        headers.add("te");
        headers.add("trailers");
        headers.add("transfer-encoding");
        headers.add("upgrade");
        headers.add("set-cookie");
        DEFAULT_RESPONSE_HEADERS_TO_IGNORE = headers;
    }

    private final net.spy.memcached.MemcachedClient client;
    private final int headersLength;
    private final CacheKeyCreator cacheKeyCreator;
    private final int defaultExpiryInSeconds;
    private final Map<String,String> responseHeadersToIgnore;



    public SpyFilterMemcachedStorage(MemcachedClient client, CacheKeyCreator cacheKeyCreator) {
        this(client,cacheKeyCreator,8192);
    }

    public SpyFilterMemcachedStorage(MemcachedClient client, CacheKeyCreator cacheKeyCreator, Map<String,String> headersToIgnore) {

    public SpyFilterMemcachedStorage(MemcachedClient client, CacheKeyCreator cacheKeyCreator, Map<String,String> headersToIgnore) {
        this(client,cacheKeyCreator,headersToIgnore)
    }

    public

    public SpyFilterMemcachedStorage(MemcachedClient client, CacheKeyCreator cacheKeyCreator, int headersLength,
                                     Map<String,String> headersToIgnore, int defaultExpiryInSeconds) {
        this.headersLength = headersLength;
        this.client = client;
        this.cacheKeyCreator = cacheKeyCreator;
        this.responseHeadersToIgnore = headersToIgnore;
        this.defaultExpiryInSeconds = defaultExpiryInSeconds;

    }

    public void writeToCache(HttpServletRequest theRequest, BufferedResponseWrapper theResponse) {

//        ResizeableByteBuffer bufferedContent = servletResponse.getBufferedMemcachedContent();
//        boolean shouldWriteToMemcached = bufferedContent.canWrite();
//        bufferedContent.closeForWrites();
//        if(bufferedContent!=null && shouldWriteToMemcached) {
//            String key = createKey(servletRequest);
//            filterMemcachedStorage.writeToCache(key, 10, Collections.EMPTY_SET, getHeaders(DEFAULT_HEADERS_TO_IGNORE, servletResponse), servletResponse.getBufferedMemcachedContent());
//        }
    }

    @Override
    public void writeToCache(String key, int expiryInSeconds, Set<String> additionalContent,
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
