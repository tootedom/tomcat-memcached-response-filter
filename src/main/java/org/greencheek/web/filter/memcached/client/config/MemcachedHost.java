package org.greencheek.web.filter.memcached.client.config;

/**
 * Created by dominictootell on 07/04/2014.
 */
public class MemcachedHost {
    private final String host;
    private final int port;

    public MemcachedHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
