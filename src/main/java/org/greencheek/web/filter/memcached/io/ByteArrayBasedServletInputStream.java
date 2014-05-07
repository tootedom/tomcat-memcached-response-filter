package org.greencheek.web.filter.memcached.io;

import javax.servlet.ServletInputStream;


public class ByteArrayBasedServletInputStream extends ServletInputStream {
    private ThreadUnsafeByteArrayInputStream input;

    public ByteArrayBasedServletInputStream(byte[] alreadyReadContent) {
        input = new ThreadUnsafeByteArrayInputStream(alreadyReadContent);
    }

    @Override
    public int read() {
        return input.read();
    }
}