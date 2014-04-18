package org.greencheek.web.filter.memcached.client;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class RegexMaxAgeParser implements MaxAgeParser {
    private static final Pattern MAX_AGE_PATTERN = Pattern.compile("max-age=(\\d+)");
    private static final String MAX_AGE_STR = "max-age=";

    @Override
    public int maxAge(String header, int defaultExpiry) {
        Matcher m = MAX_AGE_PATTERN.matcher(header);
        if(m.find()) {
           return Integer.parseInt(m.group(1));
        } else {
            return defaultExpiry;
        }
    }
}
