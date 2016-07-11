package com.circloop.deviceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.circloop.database.Child;
import com.circloop.database.Group;
import com.circloop.database.MyDatabaseHelper;
import com.circloop.deviceDiscover.AutoDiscover;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import static jxl.Workbook.getWorkbook;

public class HomeActivity extends Activity implements OnChildClickListener {
    public static MyDatabaseHelper dbHelper;
    public static ExpandableListView mListView = null;
    private static ExpandAdapter mAdapter = null;
    private List<List<Child>> mData = new ArrayList<List<Child>>();
    private Button add_group_button;
    private static List<Group> groupList;//数据库内所有分组数据存入groupList
    private HashMap<String,Child> deviceMap=new HashMap<String, Child>();//数据库内所有设备数据存入deviceMap,key为ip,值为Child
    public static Map<String,String> oidMap=new HashMap<String, String>();

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        dbHelper=new MyDatabaseHelper(this,"Device.db3",null,1);
        initData();
        mListView = (ExpandableListView) findViewById(R.id.listView);
        add_group_button= (Button) findViewById(R.id.btnAddGroup);

        //箭头设置
        mListView.setGroupIndicator(getResources().getDrawable(
                R.drawable.expander_floder));

        mAdapter = new ExpandAdapter(this,groupList, mData);
        mListView.setAdapter(mAdapter);
        mListView.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
        mListView.setChildDivider(getResources().getDrawable(R.drawable.child_divider));
        mListView.setOnChildClickListener(this);
        //点击跳到添加分组界面
        add_group_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, add_group.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    /*
     * ChildView 设置 布局很可能onChildClick进不来，要在 ChildView layout 里加上
     * android:descendantFocusability="blocksDescendants",
     * 还有isChildSelectable里返回true
     */

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        // TODO Auto-generated method stub
        Child child = mAdapter.getChild(groupPosition, childPosition);
        new AlertDialog.Builder(this)
                .setTitle(child.getIp())
                .setMessage(child.getOid())
                .setIcon(android.R.drawable.ic_menu_more)
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub

                            }
                        }).create().show();
        return true;
    }
    private void initData() {
        initialOidMap();
        String sql_group="select * from group_info";
        String sql_child="select * from device_info";
        Cursor cursor=dbHelper.getWritableDatabase().rawQuery(sql_group, null);
        groupList=new ArrayList<Group>();
        while (cursor.moveToNext()){
            Group group=new Group(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),false,0,Integer.valueOf(cursor.getString(5)));
            groupList.add(group);
        }

        cursor=dbHelper.getWritableDatabase().rawQuery(sql_child,null);
        while (cursor.moveToNext()){
            Child child=new Child(cursor.getString(1),cursor.getString(2),cursor.getString(3));
            deviceMap.put(cursor.getString(2),child);
        }
        Set<String> deciceIps=deviceMap.keySet();
        for(int i=0;i<groupList.size();i++){
            List<Child> children=new ArrayList<Child>();
            List<String> ips=getIpfromBeginToEnd(groupList.get(i).getIpBegin(),groupList.get(i).getIpEnd());
            for(String ip:ips){
                if(deciceIps.contains(ip)){
                    if(!deviceMap.get(ip).getDeviceType().equals("UnknownDevice"))
                        children.add(0,deviceMap.get(ip));
                    else
                        children.add(deviceMap.get(ip));
                }
            }
            mData.add(children);
        }
    }
    private void initialOidMap(){
        try {
            Workbook book= getWorkbook(HomeActivity.this.getResources().getAssets().open("oidList.xls"));
            Sheet sheet=book.getSheet(0);
//            System.out.println(sheet.getColumns());
            int rows=sheet.getRows();
            for(int i=0;i<rows;i++){
                oidMap.put(sheet.getCell(0,i).getContents(),sheet.getCell(1,i).getContents());
            }
            book.close();
//            System.out.println(oidMap.keySet());
//            System.out.println(oidMap.values());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }
    private List<String> getIpfromBeginToEnd(String fromIp,String toIp){
        List<String> ips=new ArrayList<String>();
        String[] begin=fromIp.split("\\.");
        String[] end=toIp.split("\\.");
        int[] current=new int[4];
        for(int i=0;i<4;i++)
            current[i]=Integer.parseInt(begin[i]);
        String currentString=fromIp;
        while(!currentString.equals(toIp)){
            currentString=current[0]+"."+current[1]+"."+current[2]+"."+current[3];
            ips.add(currentString);
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
        return ips;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==0 && resultCode==0){
            Bundle data=intent.getExtras();
            String group_name=data.getString("group_name");
            String group_desc=data.getString("group_desc");
            String ip_begin=data.getString("ip_begin");
            String ip_end=data.getString("ip_end");
            int ipNums=data.getInt("ip_nums");
            Group group = new Group(group_name,group_desc,ip_begin,ip_end,true, 0,ipNums);
            group.progressBarMax=ipNums;
            groupList.add(group);
            mData.add(new ArrayList<Child>());
            ExpandAdapter.setIndexOfNewGroup(groupList.size() - 1);//为了避免在同时添加两个分组时，前一个分组的数据添加到了后一个分组中。
            HomeActivity.getAdapter().notifyDataSetChanged();
            AutoDiscover.scan(ip_begin, ip_end, mData,groupList.size()-1);
        }
        else if(requestCode==0 && resultCode==1){//未成功添加分组，不做任何处理
        }
    }
    public static ExpandAdapter getAdapter(){
        return mAdapter;
    }
    public static List<Group> getGroupList(){
        return groupList;
    }
}