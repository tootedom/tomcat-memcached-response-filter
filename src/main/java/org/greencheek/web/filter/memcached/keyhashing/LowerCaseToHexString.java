package org.greencheek.web.filter.memcached.keyhashing;

/**
 * Created by dominictootell on 09/04/2014.
 */
public class LowerCaseToHexString implements ToHexString {
    private final static char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final LowerCaseToHexString INSTANCE = new LowerCaseToHexString();

    @Override
    public String bytesToHex(byte[] data) {
        return bytesToHex(data,DIGITS_LOWER);
    }
}
