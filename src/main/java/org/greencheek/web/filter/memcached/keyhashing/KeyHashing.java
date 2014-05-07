package org.greencheek.web.filter.memcached.keyhashing;

/**
 * Created by dominictootell on 08/04/2014.
 */
public interface KeyHashing {
    public static final String MD5 = "MD5";
    public static final String SHA526  = "SHA-256";

    public String hash(String key);
    public String hash(byte[] bytes, int offset, int length);
}
