package org.greencheek.web.filter.memcached.client.spy;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import org.greencheek.web.filter.memcached.client.config.MemcachedFactoryUtils;
import org.greencheek.web.filter.memcached.client.spy.extensions.SerializingTranscoder;
import org.greencheek.web.filter.memcached.client.spy.extensions.connection.CustomConnectionFactoryBuilder;
import org.greencheek.web.filter.memcached.client.spy.extensions.connection.NoValidationConnectionFactory;
import org.greencheek.web.filter.memcached.client.spy.extensions.hashing.JenkinsHash;
import org.greencheek.web.filter.memcached.domain.Duration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class SpyMemcachedBuilder {

    private final ConnectionFactoryBuilder builder = new CustomConnectionFactoryBuilder();

    public SpyMemcachedBuilder() {
        // Jenkins Hash is the same one used by php
        builder.setHashAlg(new JenkinsHash());
        builder.setProtocol(ConnectionFactoryBuilder.Protocol.TEXT);
        builder.setReadBufferSize(DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE);
        builder.setFailureMode(FailureMode.Redistribute);
        builder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        builder.setTranscoder(new SerializingTranscoder());
    }

    private Duration memcachedHostDNSTimeout = MemcachedFactoryUtils.DEFAULT_DNS_TIMEOUT;
    private Duration memcachedPingHostTimeout = MemcachedFactoryUtils.ONE_SECOND;
    private String memcachedHosts = "localhost:11211";

    /**
     * builds against localhost
     * @return
     */
    public net.spy.memcached.MemcachedClient build() {

        List<InetSocketAddress> addresses = MemcachedFactoryUtils.getAddressableMemcachedHosts(memcachedHostDNSTimeout,
                memcachedPingHostTimeout,memcachedHosts);

        if(addresses.size()==0) {
            return null;
        } else {
            return build(addresses);
        }
    }

    private net.spy.memcached.MemcachedClient build(List<InetSocketAddress> hostsToUse) {
        try {
            return new net.spy.memcached.MemcachedClient(builder.build(), hostsToUse);
        } catch (Exception e) {
            return null;
        }
    }

    public SpyMemcachedBuilder setUseBinaryProtocol(boolean useBin) {
        if(useBin) {
            builder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);
        } else {
            builder.setProtocol(ConnectionFactoryBuilder.Protocol.TEXT);
        }
        return this;
    }

    public SpyMemcachedBuilder setProtocol(String protocol) {
        if(protocol==null) return this;
        if(protocol.equalsIgnoreCase("binary")) {
            return setUseBinaryProtocol(true);
        } else {
            return setUseBinaryProtocol(false);
        }
    }

    public SpyMemcachedBuilder setReadBufferSize(String readBufferSize) {
        try {
            builder.setReadBufferSize(Integer.parseInt(readBufferSize));
        } catch(NumberFormatException e) {

        }
        return this;
    }

    public SpyMemcachedBuilder setConsistent(String consistentType) {
        if(consistentType == null) return this;
        if(consistentType.equalsIgnoreCase("consistent"))  {
            builder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        } else if(consistentType.equalsIgnoreCase("arraymod")) {
            builder.setLocatorType(ConnectionFactoryBuilder.Locator.ARRAY_MOD);
        }

        return this;
    }

    public SpyMemcachedBuilder setFailureMode(String failureMode) {
        if(failureMode == null) return this;
        if(failureMode.equalsIgnoreCase("redistribute")) {
            builder.setFailureMode(FailureMode.Redistribute);
        } else if(failureMode.equalsIgnoreCase("retry"))  {
            builder.setFailureMode(FailureMode.Retry);
        } else if(failureMode.equalsIgnoreCase("cancel")) {
            builder.setFailureMode(FailureMode.Cancel);
        }
        return this;
    }

    public SpyMemcachedBuilder setMemcachedHosts(String hosts) {
        if(hosts!=null && hosts.trim().length()>0) {
            memcachedHosts = hosts;
        }
        return this;
    }
}
