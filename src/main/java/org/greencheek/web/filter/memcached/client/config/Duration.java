package org.greencheek.web.filter.memcached.client.config;

import java.util.concurrent.TimeUnit;

public class Duration {
    private final long duration;
    private final TimeUnit unit;

    public Duration(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;

    }

    public long toMillis() {
        return TimeUnit.MILLISECONDS.convert(duration, unit);
    }

    public long toSeconds() {
        return TimeUnit.SECONDS.convert(duration, unit);
    }
}
