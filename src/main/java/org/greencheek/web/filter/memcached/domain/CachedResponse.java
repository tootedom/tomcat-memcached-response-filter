package org.greencheek.web.filter.memcached.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by dominictootell on 14/04/2014.
 */
public class CachedResponse {
    public static final CachedResponse MISS = new CachedResponse(false, 404,Collections.EMPTY_MAP,new byte[0],0);
    public static final CachedResponse TIMED_OUT = new CachedResponse(false,504,Collections.EMPTY_MAP,new byte[0],0);

    private final boolean hit;
    private final int statusCode;
    private final Map<String,Collection<String>> headers;
    private final byte[] content;
    private final int contentOffset;

    public CachedResponse(boolean hit, int statusCode, Map<String,Collection<String>> headers,
                          byte[] content, int offset) {
        this.hit = hit;
        this.statusCode = statusCode;
        this.headers = headers;
        this.content = content;
        this.contentOffset = offset;
    }

    public boolean isCacheHit() {
        return this.hit;
    }

    public boolean isCacheMiss() {
        return !this.hit;
    }

    public byte[] getContent() {
        return content;
    }



    public Map<String,Collection<String>> getHeaders() {
        return headers;
    }

    public int getContentOffset() {
        return contentOffset;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
