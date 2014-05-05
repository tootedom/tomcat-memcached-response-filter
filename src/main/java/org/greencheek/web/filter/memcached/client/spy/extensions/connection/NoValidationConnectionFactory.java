package org.greencheek.web.filter.memcached.client.spy.extensions.connection;

import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.MemcachedConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by dominictootell on 03/05/2014.
 */
public class NoValidationConnectionFactory extends DefaultConnectionFactory {

    public MemcachedConnection createConnection(List<InetSocketAddress> addrs)
            throws IOException {
        return new NoKeyValidationMemcachedConnection(getReadBufSize(), this, addrs,
                getInitialObservers(), getFailureMode(), getOperationFactory());
    }

    @Override
    public String toString() {
        return "Failure Mode: " + getFailureMode().name() + ", Hash Algorithm: "
                + ((HashAlgorithm)getHashAlg()).getClass().getName() + " Max Reconnect Delay: "
                + getMaxReconnectDelay() + ", Max Op Timeout: " + getOperationTimeout()
                + ", Op Queue Length: " + getOpQueueLen() + ", Op Max Queue Block Time"
                + getOpQueueMaxBlockTime() + ", Max Timeout Exception Threshold: "
                + getTimeoutExceptionThreshold() + ", Read Buffer Size: "
                + getReadBufSize() + ", Transcoder: " + getDefaultTranscoder()
                + ", Operation Factory: " + getOperationFactory() + " isDaemon: "
                + isDaemon() + ", Optimized: " + shouldOptimize() + ", Using Nagle: "
                + useNagleAlgorithm() + ", ConnectionFactory: " + getName();
    }
}
