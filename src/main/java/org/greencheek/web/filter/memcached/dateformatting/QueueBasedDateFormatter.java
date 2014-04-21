package org.greencheek.web.filter.memcached.dateformatting;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by dominictootell on 19/04/2014.
 */
public class QueueBasedDateFormatter implements DateHeaderFormatter {
    private static final TimeZone gmtZone = TimeZone.getTimeZone("GMT");
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
        SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_DATE, Locale.US);
        /**
         * GMT timezone - all HTTP dates are on GMT
         */
        sdf.setTimeZone(gmtZone);
        return sdf;

    }
}
