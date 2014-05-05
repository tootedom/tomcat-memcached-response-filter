package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.domain.Duration;
import org.greencheek.web.filter.memcached.domain.MemcachedHost;
import org.greencheek.web.filter.memcached.io.util.dns.lookup.AddressChecker;
import org.greencheek.web.filter.memcached.io.util.dns.lookup.LookupService;
import org.greencheek.web.filter.memcached.io.util.dns.lookup.TCPAddressChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Set of utility functions that test and parse a list of memcached hosts
 */
public class MemcachedFactoryUtils {
    private static Logger logger = LoggerFactory.getLogger(MemcachedFactoryUtils.class);
    public static final int DEFAULT_MEMCACHED_PORT = 11211;
    public static final Duration DEFAULT_DNS_TIMEOUT = new Duration(3,TimeUnit.SECONDS);
    public static final Duration ONE_SECOND = new Duration(1,TimeUnit.SECONDS);


    private static List<InetSocketAddress> validateMemcacheHosts(List<InetSocketAddress> addressesToCheck) {
        return validateMemcacheHosts(ONE_SECOND,addressesToCheck);
    }

    private static List<InetSocketAddress> validateMemcacheHosts(Duration checkTimeout,
                                                                 List<InetSocketAddress> addressesToCheck) {
        List<InetSocketAddress> okAddresses = new ArrayList<InetSocketAddress>();
        AddressChecker addressChecker = new TCPAddressChecker(checkTimeout.toMillis());
        for(InetSocketAddress addy : addressesToCheck) {
            if(addressChecker.isAvailable(addy)) {
                okAddresses.add(addy);
            } else {
              logger.error("Unable to connect to memcached node: {}", addy);
            }
        }
        return okAddresses;
    }

    private static List<InetSocketAddress> returnSocketAddressesForHostNames(List<MemcachedHost> nodes) {
        return returnSocketAddressesForHostNames(nodes,DEFAULT_DNS_TIMEOUT);
    }

    /**
     * Takes the list of host and port pairs, interating over each in turn and attempting:
     * resolve the hostname to an ip, and attempting a connection to the host on the given port
     *
     *
     * @param nodes The list of ports to connect
     * @param dnsLookupTimeout The amount of time to wait for a dns lookup to take.
     * @return
     */
    private static List<InetSocketAddress> returnSocketAddressesForHostNames(List<MemcachedHost> nodes,
                                                                      Duration dnsLookupTimeout) {
        LookupService addressLookupService = LookupService.create();

        List<InetSocketAddress> workingNodes = new ArrayList<InetSocketAddress>(nodes.size());
        for (MemcachedHost hostAndPort : nodes) {
            Future<InetAddress> future = null;
            String host = hostAndPort.getHost();
            int port = hostAndPort.getPort();
            try {
                future = addressLookupService.getByName(host);
                InetAddress ia = future.get(dnsLookupTimeout.toSeconds(), TimeUnit.SECONDS);
                if (ia == null) {
                    logger.error("Unable to resolve dns entry for the host: {}", host);
                }
                else
                {
                    try {
                        workingNodes.add(new InetSocketAddress(ia,port));
                    }
                    catch (IllegalArgumentException e) {
                        logger.error("Invalid port number has been provided for the memcached node: host({}),port({})", host, port);
                    }
                }
            }
            catch(TimeoutException e) {
                    logger.error("Problem resolving host name ({}) to an ip address in fixed number of seconds: {}", host, dnsLookupTimeout, e);
            }
            catch(Exception e) {
                    logger.error("Problem resolving host name to ip address: {}", host,e);
            }
            finally {
                if (future != null) future.cancel(true);
            }
        }
        addressLookupService.shutdown();

        return workingNodes;
    }

    /**
     * Takes a string:
     *
     * url:port,url:port
     *
     * converting it to a list of 2 element string arrays:  [url,port],[url,port]
     *
     * @param urls
     * @return
     */
    private static List<MemcachedHost> parseMemcachedNodeList(String urls) {
        if (urls == null) return Collections.EMPTY_LIST;
        String hostUrls = urls.trim();
        List<MemcachedHost> memcachedNodes = new ArrayList<MemcachedHost>(4);
        for (String url : hostUrls.split(",")) {
            int port = DEFAULT_MEMCACHED_PORT;
            int indexOfPort = url.indexOf(':');
            String host;
            if(indexOfPort==-1) {
                host = url.trim();
            } else {
                host = url.substring(0,indexOfPort).trim();
            }

            try {
                port = Integer.parseInt(url.substring(indexOfPort + 1, url.length()));
                if(port > 65535) {
                    port = DEFAULT_MEMCACHED_PORT;
                }
            }
            catch ( NumberFormatException e) {
                logger.info("Unable to parse memcached port number, not an integer");
            }

            if ( host.length() != 0 ) {
                memcachedNodes.add(new MemcachedHost(host, port));
            }
        }
        return memcachedNodes;
    }

    /**
     *
     * @param hosts
     * @return
     */
    public static List<InetSocketAddress> getAddressableMemcachedHosts(String hosts) {
        List<MemcachedHost> memcachedHosts = parseMemcachedNodeList(hosts);
        List<InetSocketAddress> resolvedMemcachedHosts = returnSocketAddressesForHostNames(memcachedHosts);
        return validateMemcacheHosts(resolvedMemcachedHosts);
    }

    public static List<InetSocketAddress> getAddressableMemcachedHosts(Duration dnsLookuptimeout, Duration pingCheckTimeout,
                                                                String hosts,boolean checkConnectivity) {
        List<MemcachedHost> memcachedHosts = parseMemcachedNodeList(hosts);
        List<InetSocketAddress> resolvedMemcachedHosts = returnSocketAddressesForHostNames(memcachedHosts,dnsLookuptimeout);
        if(checkConnectivity) {
            return validateMemcacheHosts(pingCheckTimeout, resolvedMemcachedHosts);
        } else {
            return resolvedMemcachedHosts;
        }
    }

}
