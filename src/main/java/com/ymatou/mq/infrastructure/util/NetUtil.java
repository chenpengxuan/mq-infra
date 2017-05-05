/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.mq.infrastructure.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 网络辅助类
 * 
 * @author wangxudong 2016年8月2日 上午11:40:56
 *
 */
public class NetUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

    private static volatile String localIp;

    private NetUtil( ) {

    }


    public static String getHostIp( ) {
        if (localIp != null) {
            return localIp;
        }
        synchronized (NetUtil.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    localIp = "127.0.0.1";
                    logger.error("Failed to extract local ip. use 127.0.0.1 instead. {}", t.getMessage(), t);
                }
            }

            if (localIp == null ) {
                localIp = "127.0.0.1";
                logger.error("Failed to extract local ip. use 127.0.0.1 instead");
            }

            return localIp;
        }
    }
}
