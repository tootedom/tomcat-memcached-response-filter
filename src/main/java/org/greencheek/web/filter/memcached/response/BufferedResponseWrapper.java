package org.greencheek.web.filter.memcached.response;

import org.apache.catalina.connector.Constants;
import org.apache.catalina.connector.CoyoteOutputStream;
import org.apache.catalina.connector.CoyoteWriter;
import org.apache.tomcat.util.res.StringManager;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferOutputStream;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferWithOverflowMarker;
import org.greencheek.web.filter.memcached.io.ResizeableByteBufferWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {
    protected HttpServletResponse origResponse = null;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);

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

    private ResizeableByteBufferWithOverflowMarker bufferedMemcachedContent;

    /**
     * The associated writer.
     */
    protected ResizeableByteBufferWriter writer;

    private final int memcachedContentBufferSize;


    public BufferedResponseWrapper(int memcachedContentBufferSize, HttpServletResponse response) {
        super(response);

        this.memcachedContentBufferSize = memcachedContentBufferSize;
        origResponse = response;
    }


    @Override
    public ServletOutputStream getOutputStream()
            throws IOException {

        if (usingWriter) {
            throw new IllegalStateException
                    (sm.getString("coyoteResponse.getOutputStream.ise"));
        }

        usingOutputStream = true;
        if (outputStream == null) {
            outputStream = new ResizeableByteBufferOutputStream(memcachedContentBufferSize, origResponse.getOutputStream());
            System.out.println("setting buffer");
            bufferedMemcachedContent = outputStream.getBuffer();
        }
        return outputStream;
    }

    public ResizeableByteBufferWithOverflowMarker getBufferedMemcachedContent() {
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
            throw new IllegalStateException
                    (sm.getString("coyoteResponse.getWriter.ise"));
        }

        usingWriter = true;
        if (writer == null) {
            writer = new ResizeableByteBufferWriter(memcachedContentBufferSize, origResponse.getWriter());
            System.out.println("setting buffer");
            bufferedMemcachedContent = writer.getBuffer();
        }
        return writer;

    }

}