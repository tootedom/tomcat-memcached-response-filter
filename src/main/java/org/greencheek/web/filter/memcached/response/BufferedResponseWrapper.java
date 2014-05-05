package org.greencheek.web.filter.memcached.response;


import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferOutputStream;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Using output stream flag.
     */
    protected boolean usingOutputStream = false;


    /**
     * Using writer flag.
     */
    protected boolean usingWriter = false;

    /**
     * The associated output stream.
     */
    protected ResizeableByteBufferOutputStream outputStream;


    /**
     *
     */
    private volatile ResizeableByteBuffer bufferedMemcachedContent;

    /**
     * The associated writer.
     */
    protected PrintWriter writer;

    private int contentLength = Integer.MIN_VALUE;

    private final int memcachedContentBufferSize;
    private final HttpServletResponse origResponse;
    private final String cacheKey;

    public BufferedResponseWrapper(int memcachedContentBufferSize, HttpServletResponse response,
                                   String cacheKey)
    {
        super(response);

        this.memcachedContentBufferSize = memcachedContentBufferSize;
        origResponse = response;
        this.cacheKey = cacheKey;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public void setContentLength(int length) {
        this.contentLength = length;
        origResponse.setContentLength(length);
    }

    public int getContentLength() {
        return contentLength;
    }


    @Override
    public ServletOutputStream getOutputStream()
            throws IOException {

        if (usingWriter) {
            throw new IllegalStateException("Outputstream already being used");
        }

        usingOutputStream = true;
        if (outputStream == null) {
            outputStream = new ResizeableByteBufferOutputStream(memcachedContentBufferSize, origResponse.getOutputStream());
            bufferedMemcachedContent = outputStream.getBuffer();
        }
        return outputStream;
    }

    public ResizeableByteBuffer getBufferedMemcachedContent() {
        return bufferedMemcachedContent;
    }

    /**
     * Return the Locale assigned to this response.
     */
    @Override
    public Locale getLocale() {
        return origResponse.getLocale();
    }


    /**
     * Return the writer associated with this Response.
     *
     * @throws IllegalStateException if <code>getOutputStream</code> has
     *                               already been called for this response
     * @throws IOException           if an input/output error occurs
     */
    @Override
    public PrintWriter getWriter()
            throws IOException {

        if (usingOutputStream) {
            throw new IllegalStateException("Writer already being used");
        }

        usingWriter = true;
        if (writer == null) {
            ResizeableByteBufferWriter bufferedWriter = new ResizeableByteBufferWriter(memcachedContentBufferSize, origResponse.getWriter());
            writer = new PrintWriter(bufferedWriter);
            bufferedMemcachedContent = bufferedWriter.getBuffer();
        }
        return writer;

    }

    @Override
    public String getHeader(String name) {
       return super.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return super.getHeaders(name);
    }


    @Override
    public Collection<String> getHeaderNames() {
        return super.getHeaderNames();
    }

    @Override
    public int getStatus() {
        return super.getStatus();
    }

}