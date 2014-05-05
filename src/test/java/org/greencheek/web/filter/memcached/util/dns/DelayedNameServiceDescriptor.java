package org.greencheek.web.filter.memcached.util.dns;

import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;


/**
 * Created by dominictootell on 05/05/2014.
 */
public class DelayedNameServiceDescriptor implements NameServiceDescriptor {

    /**
     * Returns a reference to a dnsjava name server provider.
     */
    public NameService
    createNameService() {
        return new DelayedNameService();
    }

    public String
    getType() {
        return "dns";
    }

    public String
    getProviderName() {
        return "delayeddns";
    }

}