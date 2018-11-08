package org.core.device.network;

import org.core.device.utils.Utils;

/**
 * Created by jane on 20.01.17.
 */
public class NetUtils {

    public static final String WIFI0_INTERFACE_NAME = "wlan0";
    public static final String WIFI1_INTERFACE_NAME = "wlan1";
    public static final String ETHERNET_INTERFACE_NAME_0 = "eth0";
    public static final String ETHERNET_INTERFACE_NAME_1 = "eth1";

    private static final String INET_ADDR_SIGNATURE = " inet addr:";

    public static String getInterfaceIp(String interfaceName) {
        String data = Utils.executeShellCommand("ifconfig " + interfaceName);
        if ((data == null) || (data.isEmpty())) {
            // throw new RuntimeException("Ошибка выполнения \"ifconfig \" для " + interfaceName);
            return "";
        }

        if (data.indexOf(INET_ADDR_SIGNATURE) != -1) {
            data = data.substring(data.indexOf(INET_ADDR_SIGNATURE) + INET_ADDR_SIGNATURE.length());
            data = data.substring(0, data.indexOf(' '));
        } else {
            data = "";
        }

        return data;
    }

    public static NetworkInterfacesInfo getInterfacesInfo() {
        NetworkInterfacesInfo info = new NetworkInterfacesInfo();
        info.setEth0Ip(getInterfaceIp(ETHERNET_INTERFACE_NAME_0));
        info.setEth1Ip(getInterfaceIp(ETHERNET_INTERFACE_NAME_1));
        info.setWlan0Ip(getInterfaceIp(WIFI0_INTERFACE_NAME));
        info.setWlan1Ip(getInterfaceIp(WIFI1_INTERFACE_NAME));
        return info;
    }

    public static boolean ping(String host) {
        String rslt = Utils.executeShellCommand("ping -c 1 " + host);
        return (rslt.toUpperCase().contains("TTL="));
    }

}
