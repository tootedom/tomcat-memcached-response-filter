package org.greencheek.web.filter.memcached.io;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class ResizeableByteBufferOutputStream extends ServletOutputStream {

    private final ResizeableByteBuffer buffer;
    private final OutputStream wrappedStream;

    public ResizeableByteBufferOutputStream(int initialCapacity,int maxCapacity, OutputStream wrappedStream) {
        this.wrappedStream = wrappedStream;
        this.buffer = new ResizeableByteBuffer(initialCapacity,maxCapacity);
    }

    public ResizeableByteBuffer getBuffer() {
        return buffer;
    }


    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
        this.wrappedStream.write(b);
    }

    public void close() throws IOException {
        wrappedStream.close();
    }
}
