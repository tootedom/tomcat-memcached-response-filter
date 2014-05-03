package org.greencheek.web.filter.memcached.client.spy.extensions.connection;

import net.spy.memcached.DefaultConnectionFactory;
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
        return new MemcachedConnection(getReadBufSize(), this, addrs,
                getInitialObservers(), getFailureMode(), getOperationFactory());
    }
}
