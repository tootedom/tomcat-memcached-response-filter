package org.greencheek.web.filter.memcached.dateformatting;

/**
 * Created by dominictootell on 19/04/2014.
 */
public interface DateHeaderFormatter {
    /**
     * The only date format permitted when generating HTTP headers.
     */
    public static final String RFC1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public String toDate(long millis);
}
