package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.MemcachedClient;
import org.junit.*;
import sun.misc.Service;
import sun.net.spi.nameservice.NameServiceDescriptor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by dominictootell on 05/05/2014.
 */
public class SpyMemcachedBuilderDnsTimeoutTest {


    private MemcachedClient client;

    @Before
    public void setUp() {
        System.setProperty("nameservice.enabled","true");
        System.setProperty("nameservice.delay","2500");

    }

    @After
    public void tearDown() {
        if(client!=null) {
            client.shutdown();
        }
        System.setProperty("nameservice.enabled","false");
    }

    @Test
    public void dnsLookupTimeout() {

        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();
        builder.setDNSTimeoutInSeconds("1");
        builder.setCheckHostConnectivity(false);
        builder.setMemcachedHosts("localhost:11211");

        client = builder.build();

        assertNull("client should be null",client);
    }

    @Test
    public void testLongerDnsTimeout() {
        SpyMemcachedBuilder builder = new SpyMemcachedBuilder();
        builder.setDNSTimeoutInSeconds("4");
        builder.setCheckHostConnectivity(false);
        builder.setMemcachedHosts("localhost:11211");

        client = builder.build();

        assertNotNull(client);
    }
}
