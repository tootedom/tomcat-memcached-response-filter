package org.greencheek.web.filter.memcached.io;

import org.greencheek.web.filter.memcached.io.util.CharToByteArray;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class ResizeableByteBufferWriter extends PrintWriter {

    private final ResizeableByteBufferWithOverflowMarker buffer;
    private final PrintWriter wrappedWriter;

    public ResizeableByteBufferWriter(int maxCapacity, PrintWriter wrappedWriter) {
        super(wrappedWriter);
        this.wrappedWriter = wrappedWriter;
        this.buffer = new ResizeableByteBufferWithOverflowMarker(maxCapacity);
    }

    public ResizeableByteBufferWithOverflowMarker getBuffer() {
        return buffer;
    }


    @Override
    public void write(char[] cbuf, int off, int len)  {
        try {
            buffer.write(new String(cbuf).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            buffer.write(new String(cbuf).getBytes());
        }
        wrappedWriter.write(cbuf,off,len);
    }

    @Override
    public void flush()  {
        wrappedWriter.flush();
    }

    @Override
    public void close()  {
        wrappedWriter.close();
        System.out.println("lkjlkjlkjlkj");
    }

}
