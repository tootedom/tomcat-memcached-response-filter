package org.greencheek.web.filter.memcached.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dominictootell on 07/05/2014.
 */
public class IOUtils {
    public static byte[] readStream(int bufferSize, InputStream inputStream, int contentLength) throws IOException{
        ResizableByteBufferNoBoundsCheckingBackedOutputStream output;
        output = new ResizableByteBufferNoBoundsCheckingBackedOutputStream(contentLength);
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.getBuf();
    }
}
