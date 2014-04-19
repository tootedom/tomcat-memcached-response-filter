package org.greencheek.web.filter.memcached.dataformatting;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by dominictootell on 19/04/2014.
 */
public class QueueBasedDateFormatter implements DateHeaderFormatter {
    public static final int DEFAULT_NUMBER_OF_DATE_FORMATTERS = Runtime.getRuntime().availableProcessors()*2;

    private final ArrayBlockingQueue<SimpleDateFormat> digesters;

    public QueueBasedDateFormatter() {
        this(DEFAULT_NUMBER_OF_DATE_FORMATTERS);
    }

    public QueueBasedDateFormatter(int numberOfFormatters) {
        digesters = new ArrayBlockingQueue<SimpleDateFormat>(numberOfFormatters);
        for(int i=0;i<numberOfFormatters;i++) {
            digesters.add(createFormatter());
        }
    }

    @Override
    public String toDate(long millis) {
        Date dateValue = new Date(millis);
        SimpleDateFormat dateFormat = digesters.remove();
        String d = dateFormat.format(dateValue);
        digesters.add(dateFormat);
        return d;
    }


    private SimpleDateFormat createFormatter() {
        return new SimpleDateFormat(RFC1123_DATE, Locale.US);

    }
}
