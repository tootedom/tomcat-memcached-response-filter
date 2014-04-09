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

    private final ResizeableByteBufferWithOverflowMarker buffer;
    private final OutputStream wrappedStream;
    private final CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
    private final double maxBytesPerChar = utf8Encoder.maxBytesPerChar();

    public ResizeableByteBufferOutputStream(int maxCapacity, OutputStream wrappedStream) {
        this.wrappedStream = wrappedStream;
        this.buffer = new ResizeableByteBufferWithOverflowMarker(maxCapacity);
    }

    public ResizeableByteBufferWithOverflowMarker getBuffer() {
        return buffer;
    }


    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
        this.wrappedStream.write(b);
    }

    public void closeBuffer() {
        buffer.close();
    }

    public void close() throws IOException {
        closeBuffer();
        wrappedStream.close();
    }
}
