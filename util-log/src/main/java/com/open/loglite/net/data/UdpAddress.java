package com.open.loglite.net.data;

/**
 * Created by Administrator on 2017/11/17.
 */

public class UdpAddress {
    public String  ip;
    public int     port;

    public UdpAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}