package org.greencheek.web.filter.memcached.client;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class DefaultMaxAgeParser implements MaxAgeParser {

    public static final String MAX_AGE_KEY = "max-age=";

    @Override
    public int maxAge(String header, int defaultExpiry) {
        int index = header.indexOf(MAX_AGE_KEY);
        if(index == -1) {
            return defaultExpiry;
        } else {
            return getDigits(header,index+8,header.length(),defaultExpiry);
        }
    }

    private int getDigits(String header, int offset, int len, int defaultExpiry) {
        char[] chars = new char[len - offset];
        header.getChars(offset, len, chars, 0);
        StringBuilder b = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 47 && chars[i] < 58) {
                b.append(chars[i]);
            } else {
                break;
            }
        }

        String s = b.toString();
        if (s.length() > 0) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return defaultExpiry;
            }
        } else {
            return defaultExpiry;
        }

    }
}
