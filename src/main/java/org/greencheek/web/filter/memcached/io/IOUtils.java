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
}
