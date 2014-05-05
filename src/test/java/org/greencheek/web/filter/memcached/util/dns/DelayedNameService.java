package org.greencheek.web.filter.memcached.util.dns;

import sun.net.spi.nameservice.NameService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by dominictootell on 05/05/2014.
 */
public class DelayedNameService implements NameService {
    private static final int DEFAULT_DELAY_TIME = 3000;

    @Override
    public InetAddress[] lookupAllHostAddr(String s) throws UnknownHostException {
        int delayTime = getDelayTime();

        boolean enabled = Boolean.parseBoolean(System.getProperty("nameservice.enabled","false"));
        try {
            if(enabled) {
                Thread.sleep(delayTime);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("String: " + s);

        if(!s.toLowerCase().contains("localhost.1")) {
            byte[] loopback = {0x7f,0x00,0x00,0x01};
            return new InetAddress[]{ InetAddress.getByAddress("localhost",loopback)};
        } else {
            throw new UnknownHostException("localhost.1");
        }

    }

    @Override
    public String getHostByAddr(byte[] bytes) throws UnknownHostException {
        int delayTime = getDelayTime();

        boolean enabled = Boolean.parseBoolean(System.getProperty("nameservice.enabled","false"));
        try {
            if(enabled) {
                Thread.sleep(delayTime);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return "localhost";
    }

    private int getDelayTime() {
        int delayTime;
        try {
            delayTime = Integer.parseInt(System.getProperty("nameservice.delay", ""+DEFAULT_DELAY_TIME));
        } catch(NumberFormatException exception) {
            delayTime = DEFAULT_DELAY_TIME;
        }
        return delayTime;
    }
}
