package org.greencheek.web.filter.memcached.client;

/**
 * Created by dominictootell on 18/04/2014.
 */
public interface MaxAgeParser {
    public int maxAge(String header, int defaultExpiry);
}
