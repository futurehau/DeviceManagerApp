package com.circloop.deviceDiscover;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adventnet.snmp.beans.SnmpTarget;
import com.circloop.database.Child;
import com.circloop.database.Group;
import com.circloop.database.MyDatabaseHelper;
import com.circloop.deviceManager.HomeActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by 浩思于微 on 2016/5/5.
 */
public class AutoDiscover {
    static MyDatabaseHelper dbHelper;
    static SQLiteDatabase db;
    private static IpTask ipTask;
    private static InetAddress address;
    private static int retries=1;
    private static int timeout=3000;
    private static List<Group> groupList;
    private static List<List<Child>> mData;
    //    private static ExecutorService FULL_TASK_EXECUTOR=(ExecutorService) Executors.newCachedThreadPool();//线程池大小为无限大，所以如果ip过多可能导致内存溢出
    private static ExecutorService LIMIT_TASK_EXECUTOR=(ExecutorService) Executors.newFixedThreadPool(50);//可以限定线程池的大小


    public static void scan(String fromIp, String toIp, List<List<Child>> mData1,int index){
        mData=mData1;
        dbHelper=HomeActivity.dbHelper;
        db=dbHelper.getWritableDatabase();
        groupList=HomeActivity.getGroupList();

        String[] begin=fromIp.split("\\.");
        int[] current=new int[4];
        for(int i=0;i<4;i++)
            current[i]=Integer.parseInt(begin[i]);
        String currentString=fromIp;

        while(!currentString.equals(toIp)){
            currentString=current[0]+"."+current[1]+"."+current[2]+"."+current[3];
            ipTask=new IpTask(index);
            ipTask.executeOnExecutor(LIMIT_TASK_EXECUTOR,currentString);
            current[3]++;
            if(current[3]==256){
                current[3]=0;
                current[2]++;
                if(current[2]==256){
                    current[2]=0;
                    current[1]++;
                    if(current[1]==256){
                        current[1]=0;
                        current[0]++;
                        if(current[0]==233){
                            break;
                        }
                    }
                }
            }
        }
    }
    private static class IpTask extends AsyncTask<String, Void, Boolean> {
        String ip;
        int index;
        public IpTask(int index){
            super();
            this.index=index;
        }
        @Override
        protected Boolean doInBackground(String... params) {
//            System.err.println("正在ping ip:" + params[0]);
            ip=params[0];
            int reachCount = 0;
            try {
//                System.out.println("dd"+reachCount);
                address=InetAddress.getByName(params[0]);
                for (int i = 0; i < retries; ++i) {
                    if (address.isReachable(timeout)) {
                        ++reachCount;
//                        System.out.println(ip);
                    }
                }
                if(reachCount>0){
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
//            System.err.println("结束ping ip:" + ip);

            Group group=groupList.get(index);
            group.doneIpNums++;
            int progress=group.getProgress();
            group.setProgress(progress + 1);
            if(aBoolean){
                group.pingedIps.add(ip);
//                System.out.println(ip);
            }
            if(group.doneIpNums==group.getTotalIp()){//ping结束,对ping通的设备进行设备类型发现
                List<String> pingedIps=group.pingedIps;
                group.progressBarMax=pingedIps.size();
                group.setProgress(0);
                group.setSnmpDis(true);
                for(int i=0;i<pingedIps.size();i++){
                    SnmpTask snmpTask=new SnmpTask(index);
                    snmpTask.executeOnExecutor(LIMIT_TASK_EXECUTOR,pingedIps.get(i));
                }
            }
            HomeActivity.getAdapter().notifyDataSetChanged();
        }
    }


    private static class SnmpTask extends AsyncTask<String,Void,String>{
        String targetHost=null;
        int targetPort = 161;
        String community = "public";
        int timeout = 5;
        int retries = 0;
        String oid=".1.3.6.1.2.1.1.2.0";
        String deviceOid;
        int index;
        public SnmpTask(int index){
            super();
            this.index=index;
        }
        @Override
        protected String doInBackground(String... params) {
//            System.err.println("正在snmp设备：" + params[0]);
            targetHost=params[0];
            SnmpTarget target=new SnmpTarget();
            target.setTargetHost(targetHost);
            target.setTargetPort(targetPort);
            target.setCommunity(community);
            target.setTimeout(timeout);
            target.setRetries(retries);
            target.setSnmpVersion(SnmpTarget.VERSION1);
            target.setObjectID(oid);
            deviceOid=target.snmpGet();
            return deviceOid;
        }
        @Override
        protected void onPostExecute(String params) {
            String deviceType;

            if(deviceOid==null)
                deviceType="UnknownDevice";
            else {
                deviceType="SnmpDevice";
                String[] oidArray=deviceOid.split("\\.");
                String s1;
                if(oidArray.length>=10){
                    s1=oidArray[7]+"."+oidArray[8]+"."+oidArray[9];
                    if(HomeActivity.oidMap.containsKey(s1))
                        deviceType=HomeActivity.oidMap.get(s1);
                }
                else{
                    s1=oidArray[7]+"."+oidArray[8];
                    if(HomeActivity.oidMap.containsKey(s1))
                        deviceType=HomeActivity.oidMap.get(s1);
                }
            }
//            System.err.println("结束snmp设备："+params);
            Child child=new Child(deviceType,targetHost,deviceOid);
            mData.get(index).add(child);

            Group group=groupList.get(index);
            int progress=group.getProgress();
            group.setProgress(progress+1);
            db.execSQL("insert into device_info values(null,?,?,?)", new String[]{deviceType, targetHost, deviceOid});
            HomeActivity.getAdapter().notifyDataSetChanged();
        }
    }

}

