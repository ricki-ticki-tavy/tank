package org.core.device.network;

/**
 * Created by jane on 20.01.17.
 */
public class NetworkInterfacesInfo {
    private String eth0Ip;
    private String eth1Ip;
    private String wlan0Ip;
    private String wlan1Ip;

    public String getEth0Ip() {
        return eth0Ip;
    }

    public void setEth0Ip(String eth0Ip) {
        this.eth0Ip = eth0Ip;
    }

    public String getEth1Ip() {
        return eth1Ip;
    }

    public void setEth1Ip(String eth1Ip) {
        this.eth1Ip = eth1Ip;
    }

    public String getWlan0Ip() {
        return wlan0Ip;
    }

    public String getWlan1Ip() {
        return wlan1Ip;
    }

    public void setWlan0Ip(String wlan0Ip) {
        this.wlan0Ip = wlan0Ip;
    }

    public void setWlan1Ip(String wlan1Ip) {
        this.wlan1Ip = wlan1Ip;
    }

    public NetworkInterfacesInfo() {
        eth0Ip = "";
        eth1Ip = "";
        wlan0Ip = "";
        wlan1Ip = "";
    }

    @Override
    public boolean equals(Object obj) {
        NetworkInterfacesInfo ii = (NetworkInterfacesInfo) obj;
        return ((eth0Ip.equals(ii.eth0Ip)) && (eth1Ip.equals(ii.eth1Ip)) && (wlan0Ip.equals(ii.wlan0Ip)) && (wlan1Ip.equals(ii.wlan1Ip)));
    }

    @Override
    protected Object clone() {
        NetworkInterfacesInfo rslt = new NetworkInterfacesInfo();
        rslt.setEth0Ip(eth0Ip);
        rslt.setEth1Ip(eth1Ip);
        rslt.setWlan0Ip(wlan0Ip);
        return rslt;
    }

    public NetworkInterfacesInfo(String eth0Ip, String eth1Ip, String wlan0Ip, String wlan1Ip) {
        this.eth0Ip = eth0Ip;
        this.eth1Ip = eth1Ip;
        this.wlan0Ip = wlan0Ip;
        this.wlan1Ip = wlan1Ip;
    }
}
