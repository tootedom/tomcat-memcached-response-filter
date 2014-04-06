package org.greencheek.web.filter.memcached.io.util;

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
public class CharToByteArray {

    private final CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
    private final double maxBytesPerChar = utf8Encoder.maxBytesPerChar();

    public byte[] charToByteArray(char[] cbuf, int off, int len) {
        int en = (int) (len * maxBytesPerChar);
        byte[] ba = new byte[en];
        utf8Encoder.reset();
        ByteBuffer bb = ByteBuffer.wrap(ba);
        CharBuffer cb = CharBuffer.wrap(cbuf, off, len);

        boolean bufferOk = true;
        try {
            CoderResult cr = utf8Encoder.encode(cb, bb, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = utf8Encoder.flush(bb);
            if (!cr.isUnderflow())
                cr.throwException();
        } catch (CharacterCodingException x) {
            // Substitution is always enabled,
            // so this shouldn't happen
            bufferOk = false;
        }

        if (bufferOk) {
            int pos = bb.position();
            if (ba.length == pos) {
                return ba;
            } else {
                return Arrays.copyOf(ba, pos);
            }
        }
        else {
            return null;
        }
    }
}
