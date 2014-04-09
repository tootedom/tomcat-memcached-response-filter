package org.greencheek.web.filter.memcached.keyhashing;

/**
 * Created by dominictootell on 08/04/2014.
 */
public interface ToHexString {

    public String bytesToHex(byte[] data);


    public static String bytesToHex(byte[] data, char[] chars) {

        int length = data.length;
        char[] out = new char[length << 1];

        for(int i=0,j=0;i<length;i++) {
            out[j] = chars[(0xF0 & data[i]) >>> 4];
            out[j++] = chars[0x0F & data[i]];
        }
        return new String(out);
    }
}
