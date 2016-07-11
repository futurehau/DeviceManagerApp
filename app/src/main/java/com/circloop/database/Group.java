package com.circloop.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 浩思于微 on 2016/5/17.
 */
public class Group {
    private String groupName;
    private String groupDesc;
    private String ipBegin;
    private String ipEnd;
    private boolean isDiscover;
    private int progress;
    private int totalIp;
    public List<String> pingedIps;//ping通的设备
    public int doneIpNums=0;//ping结束的设备数量
    public int progressBarMax=0;//为了进度条的显示而设置的变量
    private boolean isSnmpDis=false;

    public Group(String groupName,String groupDesc,String ipBegin,String ipEnd,boolean isDiscover,int progress,int totalIp){
        this.groupName=groupName;
        this.groupDesc=groupDesc;
        this.ipBegin=ipBegin;
        this.ipEnd=ipEnd;
        this.isDiscover=isDiscover;
        this.progress=progress;
        this.totalIp=totalIp;
        pingedIps=new ArrayList<String>();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public String getIpBegin() {
        return ipBegin;
    }

    public String getIpEnd() {
        return ipEnd;
    }
    public boolean isDiscover(){
        return isDiscover;
    }
    public int getProgress(){
        return progress;
    }
    public int getTotalIp(){return totalIp;}
    public boolean isSnmpDis(){
        return isSnmpDis;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public void setIpBegin(String ipBegin) {
        this.ipBegin = ipBegin;
    }

    public void setIpEnd(String ipEnd) {
        this.ipEnd = ipEnd;
    }
    public void setIsDiscover(boolean isDiscover){
        this.isDiscover=isDiscover;
    }
    public void setProgress(int progress){
        this.progress=progress;
    }
    public void setSnmpDis(boolean isSnmpDis){
        this.isSnmpDis=isSnmpDis;
    }

}
