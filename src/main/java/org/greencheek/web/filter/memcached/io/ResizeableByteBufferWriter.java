package org.greencheek.web.filter.memcached.io;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class ResizeableByteBufferWriter extends PrintWriter {

    private final ResizeableByteBuffer buffer;
    private final PrintWriter wrappedWriter;

    public ResizeableByteBufferWriter(int maxCapacity, PrintWriter wrappedWriter) {
        super(wrappedWriter);
        this.wrappedWriter = wrappedWriter;
        this.buffer = new ResizeableByteBuffer(maxCapacity);
    }

    public ResizeableByteBuffer getBuffer() {
        return buffer;
    }




    @Override
    public void write(char[] cbuf, int off, int len)  {
        try {
            buffer.write(new String(cbuf,off,len).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            buffer.write(new String(cbuf,off,len).getBytes());
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
    }

}
