package com.circloop.database;

/**
 * Created by 浩思于微 on 2016/5/17.
 */
public class Child{
    private String deviceType;
    private String ip;
    private String oid;
    public Child(String deviceType,String ip,String oid){
        this.deviceType=deviceType;
        this.ip=ip;
        this.oid=oid;
    }
    public String getDeviceType() {
        return deviceType;
    }

    public String getIp() {
        return ip;
    }

    public String getOid() {
        return oid;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
