package com.baidu.disconf.client.common.model;

/**
 * 实例指纹
 *
 * @author liaoqiqi
 * @version 2014-6-27
 *
 * 2017-12-08
 */
public class InstanceFingerprint {
    private String host = "";  // 本实例所在机器的IP
    private int port = 0;      // 可以表示本实例的PORT
    private String uuid = "";  // 一个实例固定的UUID

    public String getHost() {
        return host;
    }

    public InstanceFingerprint(String host, int port, String uuid) {
        this.host = host;
        this.port = port;
        this.uuid = uuid;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "InstanceFingerprint [host=" + host + ", port=" + port + "]";
    }
}