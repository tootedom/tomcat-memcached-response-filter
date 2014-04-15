package org.greencheek.web.filter.memcached.domain;

import java.util.Map;

/**
 * Created by dominictootell on 14/04/2014.
 */
public class CachedResponse {
    public final boolean hit;
    public final Map<String,String> headers;
    public final byte[] content;

    public CachedResponse(boolean hit, Map<String,String> headers, byte[] content) {
        this.hit = hit;
        this.headers = headers;
        this.content = content;
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

    public Map<String,String> getHeaders() {
        return headers;
    }
}
