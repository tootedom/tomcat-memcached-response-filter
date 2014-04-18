package org.greencheek.web.filter.memcached.io;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class ResizeableByteBufferWriter extends Writer {

    private final ResizeableByteBuffer buffer;
    private final PrintWriter wrappedWriter;

    public ResizeableByteBufferWriter(int maxCapacity, PrintWriter wrappedWriter) {
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
