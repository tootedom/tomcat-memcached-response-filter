/**
 * Copyright (C) 2006-2009 Dustin Sallings
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package org.greencheek.web.filter.memcached.client.spy.extensions;

import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.transcoders.TranscoderUtils;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * Transcoder that serializes and compresses objects.
 */
public class SerializingTranscoder extends SpyObject implements
        Transcoder<Object> {
    /**
     * Default compression threshold value.
     */
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 16384;

    protected int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;

    // General flags
    static final int SERIALIZED = 1;
    static final int COMPRESSED = 2;
    private final int maxSize;

    private final TranscoderUtils tu = new TranscoderUtils(true);

    /**
     * Get a serializing transcoder with the default max data size.
     */
    public SerializingTranscoder() {
        maxSize = CachedData.MAX_SIZE;
    }


    @Override
    public boolean asyncDecode(CachedData d) {
        if ((d.getFlags() & COMPRESSED) != 0 || (d.getFlags() & SERIALIZED) != 0) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.spy.memcached.Transcoder#decode(net.spy.memcached.CachedData)
     */
    public Object decode(CachedData d) {
        byte[] data = d.getData();
        if ((d.getFlags() & COMPRESSED) != 0) {

            try {
                data = Snappy.uncompress(data);
            } catch (IOException e) {
                getLogger().warn("Unable to Uncompress");
                data = null;
            }
//            data = decompress(d.getData());
        }
        return data;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.spy.memcached.Transcoder#encode(java.lang.Object)
     */
    public CachedData encode(Object o) {
        ResizeableByteBuffer buffer = (ResizeableByteBuffer)o;
        byte[] b = null;
        int flags = 0;
        int bufferSize = buffer.size();
        if (bufferSize > compressionThreshold) {
            byte[] compressed;
            try {
                byte[] buf = new byte[Snappy.maxCompressedLength(bufferSize)];
                int compressedByteSize = Snappy.rawCompress(buffer.getBuf(), 0, bufferSize, buf, 0);
                compressed = new byte[compressedByteSize];
                System.arraycopy(buf, 0, compressed, 0, compressedByteSize);
            } catch (IOException e) {
                throw new RuntimeException("IO exception compressing data", e);
            }
            if (compressed.length < bufferSize) {
                getLogger().debug("Compressed %s from %d to %d",
                        o.getClass().getName(), bufferSize, compressed.length);
                b = compressed;
                flags |= COMPRESSED;
            } else {

                b = buffer.toByteArray();
                getLogger().info("Compression increased the size of %s from %d to %d",
                        o.getClass().getName(), b.length, compressed.length);
            }
        } else {
            b = buffer.toByteArray();
        }
        return new CachedData(flags, b, getMaxSize());
    }
}
