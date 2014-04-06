package org.greencheek.web.filter.memcached.config;

import org.greencheek.web.filter.memcached.io.util.dns.lookup.AddressChecker;
import org.greencheek.web.filter.memcached.io.util.dns.lookup.TCPAddressChecker;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class MemcachedFactoryUtils {


    private static class Duration {
        private final long length;
        private final TimeUnit unit;
        public Duration(long length, TimeUnit unit) {
            this.length = length;
            this.unit = unit;

        }

        public long toMillis() {
            return TimeUnit.MILLISECONDS.convert(length,unit);
        }
    }

    private final Duration DEFAULT_EXPIRY = new Duration(60,TimeUnit.MINUTES);
    private final int DEFAULT_MEMCACHED_PORT = 11211;
    private final Duration DEFAULT_DNS_TIMEOUT = new Duration(3,TimeUnit.SECONDS);
    private final Duration ONE_SECOND = new Duration(1,TimeUnit.SECONDS);


    private List<InetSocketAddress> validateMemcacheHosts(Duration checkTimeout,
                                      List<InetSocketAddress> addressesToCheck) {
        List<InetSocketAddress> okAddresses = new ArrayList<InetSocketAddress>();
        AddressChecker addressChecker = new TCPAddressChecker(checkTimeout.toMillis());
        for(InetSocketAddress addy : addressesToCheck) {
            if(addressChecker.isAvailable(addy)) {
                okAddresses.add(addy);
            } else {

//                    logger.error("Unable to connect to memcached node: {}", addy)

            }
        }
        return okAddresses;
    }

//    /**
//     * Takes the list of host and port pairs, interating over each in turn and attempting:
//     * resolve the hostname to an ip, and attempting a connection to the host on the given port
//     *
//     *
//     * @param nodes The list of ports to connect
//     * @param dnsLookupTimeout The amount of time to wait for a dns lookup to take.
//     * @return
//     */
//    private List<InetSocketAddress> returnSocketAddressesForHostNames(nodes: List[(String,Int)],
//    dnsLookupTimeout : Duration = DEFAULT_DNS_TIMEOUT): List[InetSocketAddress] = {
//        val addressLookupService = LookupService.create()
//
//        var workingNodes: List[InetSocketAddress] = Nil
//        for (hostAndPort <- nodes) {
//            var future: java.util.concurrent.Future[InetAddress] = null
//            val host = hostAndPort._1
//            val port = hostAndPort._2
//            try {
//                future = addressLookupService.getByName(host)
//                var ia: InetAddress = future.get(dnsLookupTimeout.toSeconds, TimeUnit.SECONDS)
//                if (ia == null) {
//                    logger.error("Unable to resolve dns entry for the host: {}", host)
//                }
//                else
//                {
//                    try {
//                        workingNodes = new InetSocketAddress(ia,port ) :: workingNodes
//                    }
//                    catch {
//                    case e: IllegalArgumentException => {
//                        logger.error("Invalid port number has been provided for the memcached node: host({}),port({})", host, port)
//                    }
//                }
//                }
//            }
//            catch {
//                case e: TimeoutException => {
//                    logger.error("Problem resolving host name ({}) to an ip address in fixed number of seconds: {}", host, dnsLookupTimeout, e)
//                }
//                case e: Exception => {
//                    logger.error("Problem resolving host name to ip address: {}", host)
//                }
//            }
//            finally {
//                if (future != null) future.cancel(true)
//            }
//        }
//        addressLookupService.shutdown()
//
//        workingNodes
//    }
//
//    /**
//     * Takes a string:
//     *
//     * url:port,url:port
//     *
//     * converting it to a list of 2 element string arrays:  [url,port],[url,port]
//     *
//     * @param urls
//     * @return
//     */
//    private def parseMemcachedNodeList(urls: String): List[(String,Int)] = {
//        if (urls == null) return Nil
//        val hostUrls = urls.trim
//        var memcachedNodes : List[(String,Int)] = Nil
//        for (url <- hostUrls.split(",")) {
//            var port: Int = DEFAULT_MEMCACHED_PORT
//            val indexOfPort = url.indexOf(':')
//            val host =  indexOfPort match {
//                case -1 => {
//                    url.trim
//                }
//                case any => {
//                    url.substring(0, any).trim
//                }
//            }
//
//            try {
//                port = Integer.parseInt(url.substring(indexOfPort + 1, url.length))
//                if(port > 65535) {
//                    port = DEFAULT_MEMCACHED_PORT
//                }
//            }
//            catch {
//                case e: NumberFormatException => {
//                    logger.info("Unable to parse memcached port number, not an integer")
//                }
//            }
//
//            if ( host.length != 0 ) {
//                memcachedNodes = (host, port) :: memcachedNodes
//            }
//        }
//        return memcachedNodes
//    }
}
