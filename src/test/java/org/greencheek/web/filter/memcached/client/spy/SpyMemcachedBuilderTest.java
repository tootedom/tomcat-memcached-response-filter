package org.greencheek.web.filter.memcached.client.spy;


import net.spy.memcached.MemcachedClient;

import org.greencheek.web.filter.memcached.EmbeddedTomcatServer;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonFactory;
import org.greencheek.web.filter.memcached.util.MemcachedDaemonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SpyMemcachedBuilderTest {
    MemcachedDaemonWrapper memcached;

    @Before
    public void setUp() {

        memcached = MemcachedDaemonFactory.createMemcachedDaemon(false);

        if(memcached.getDaemon()==null) {
            throw new RuntimeException("Unable to start local memcached");
        }
    }

    @After
    public void tearDown() {
        MemcachedDaemonFactory.stopMemcachedDaemon(memcached);
    }

    @Test
    public void checkForNotMemcachedClientOnDNSFailure() {

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();

        builder.setCheckHostConnectivity(true);
        builder.setMemcachedHosts("localhost.1:11211");

        MemcachedClient client = builder.build();

        assertNull(client);
    }

    @Test
    public void checkMemcachedConnectivity() {

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();

        builder.setCheckHostConnectivity(true);
        builder.setMemcachedHosts("localhost:"+memcached.getPort());

        MemcachedClient client = builder.build();

        assertNotNull(client);
    }

    @Test
    public void invasiveOnMemcachedHostNotAvailable() {

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();

        builder.setCheckHostConnectivity(true);
        builder.setMemcachedHosts("127.0.0.1:1234");

        MemcachedClient client = builder.build();

        assertNull(client);
    }

    @Test
    public void nonInvasiveOnMemcachedHostNotAvailable() {

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();

        builder.setCheckHostConnectivity(false);
        builder.setMemcachedHosts("localhost:1234");

        MemcachedClient client = builder.build();

        assertNotNull(client);
    }


}