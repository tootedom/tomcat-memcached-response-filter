package org.greencheek.web.filter.memcached.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dominictootell on 07/05/2014.
 */
public class IOUtils {
    public static byte[] readStreamToBytes(int bufferSize, InputStream inputStream, int contentLength) throws IOException{
        return readStreamToResizeableByteBuffer(bufferSize,inputStream,contentLength).toByteArray();
    }

    public static ResizeableByteBuffer readStreamToResizeableByteBuffer(int bufferSize, InputStream inputStream, int contentLength) throws IOException{
        ResizableByteBufferNoBoundsCheckingBackedOutputStream output;
        output = new ResizableByteBufferNoBoundsCheckingBackedOutputStream(bufferSize,contentLength);
        byte[] buffer = new byte[bufferSize];
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.getBuffer();
    }

    public static byte[] readStreamToBytes(InputStream inputStream, int contentLength) throws IOException{
        return readStreamToResizeableByteBuffer(inputStream,contentLength).toByteArray();
    }

    public static ResizeableByteBuffer readStreamToResizeableByteBuffer(InputStream inputStream, int contentLength) throws IOException{
        ResizableByteBufferNoBoundsCheckingBackedOutputStream output;
        output = new ResizableByteBufferNoBoundsCheckingBackedOutputStream(contentLength,contentLength);
        byte[] buffer = output.getBuf();
        int n;
        int offset = 0;
        int length = contentLength;
        while (length>0 && -1 != (n = inputStream.read(buffer,offset,length))) {
            length-=n;
            offset+=n;
        }
        ResizeableByteBuffer rbuffer = output.getBuffer();
        rbuffer.setSize(offset);
        return rbuffer;
    }
}
