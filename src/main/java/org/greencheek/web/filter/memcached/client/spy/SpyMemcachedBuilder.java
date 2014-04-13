package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import org.greencheek.web.filter.memcached.client.spy.extensions.SerializingTranscoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyMemcachedBuilder {

    private final ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();

    public SpyMemcachedBuilder() {
        builder.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH);
        builder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);
        builder.setReadBufferSize(DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE);
        builder.setFailureMode(FailureMode.Redistribute);
        builder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        builder.setTranscoder(new SerializingTranscoder());
    }

    /**
     * builds against localhost
     * @return
     */
    public net.spy.memcached.MemcachedClient build() {
        return build(Collections.singletonList(new InetSocketAddress(InetAddress.getLoopbackAddress(), 11211)));
    }

    public net.spy.memcached.MemcachedClient build(List<InetSocketAddress> hostsToUse) {
        try {
            return new net.spy.memcached.MemcachedClient(builder.build(), hostsToUse);
        } catch (Exception e) {
            return null;
        }
    }
}
