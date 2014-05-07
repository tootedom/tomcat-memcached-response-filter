package org.greencheek.web.filter.memcached.request;

import org.greencheek.web.filter.memcached.io.ByteArrayBasedServletInputStream;
import org.greencheek.web.filter.memcached.io.IOUtils;
import org.greencheek.web.filter.memcached.io.ResizableByteBufferNoBoundsCheckingBackedOutputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by dominictootell on 06/05/2014.
 */
public class MultiReadRequestWrapper extends HttpServletRequestWrapper {
    private volatile byte[] cachedBytes;

    private final int contentLength;
    private final HttpServletRequest originalRequest;

    public MultiReadRequestWrapper(HttpServletRequest originalRequest) {
        super(originalRequest);
        contentLength = originalRequest.getContentLength();
        this.originalRequest = originalRequest;

    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null) {
            cachedBytes = cacheInputStreamContent();
        }

        return new ByteArrayBasedServletInputStream(cachedBytes);
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        if(cachedBytes == null) {
           cachedBytes = cacheInputStreamContent();
        }

        return new BufferedReader(new InputStreamReader(new ByteArrayBasedServletInputStream(cachedBytes)));
    }


    private byte[] cacheInputStreamContent() throws IOException {
        InputStream inputStream = originalRequest.getInputStream();
        return IOUtils.readStream(4096,inputStream,contentLength);
    }



}
