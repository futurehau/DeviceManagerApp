package com.circloop.deviceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.circloop.database.MyDatabaseHelper;
import java.util.HashSet;
/**
 * Created by 浩思于微 on 2016/5/12.
 */
public class add_group extends Activity{
    private EditText et_group_name;
    private EditText et_group_desc;
    private EditText et_ip_begin;
    private EditText et_ip_end;
    private String group_name;
    private String group_desc;
    private String ip_begin;
    private String ip_end;
    private int ipNums=0;
    MyDatabaseHelper dbHelper=HomeActivity.dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group_layout);
        Button button= (Button) findViewById(R.id.btn_add_group);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_group_name = (EditText) findViewById(R.id.et_group_name);
                et_group_desc = (EditText) findViewById(R.id.et_group_desc);
                et_ip_begin = (EditText) findViewById(R.id.et_ip_begin);
                et_ip_end = (EditText) findViewById(R.id.et_ip_end);
                group_name = et_group_name.getText().toString();
                group_desc = et_group_desc.getText().toString();
                ip_begin = et_ip_begin.getText().toString();
                ip_end = et_ip_end.getText().toString();
                //新加分组信息加入数据库
                insertGroupData(dbHelper.getWritableDatabase(), group_name, group_desc, ip_begin, ip_end);
            }
        });
    }
    private void newGroupToReturn(){
        Intent intent = getIntent();
        Bundle data = new Bundle();
        data.putString("group_name", group_name);
        data.putString("group_desc", group_desc);
        data.putString("ip_begin", ip_begin);
        data.putString("ip_end", ip_end);
        data.putInt("ip_nums",ipNums);
        intent.putExtras(data);
        add_group.this.setResult(0, intent);
    }
    //新建分组加入分组数据库
    private void insertGroupData(SQLiteDatabase db,String group_name,String group_desc,String ip_begin,String ip_end){
        Cursor cursor=dbHelper.getWritableDatabase().rawQuery("select * from group_info", null);
        HashSet<String> groups=new HashSet<String>();
        while (cursor.moveToNext()){
            groups.add(cursor.getString(1));
        }
        if(checkInput()){
            if(groups.contains(et_group_name.getText().toString()))
                Toast.makeText(add_group.this, "分组已存在，请重新输入！", Toast.LENGTH_SHORT).show();
            else{
                if(isValidCIpAddress(ip_begin)){
                    if(isValidCIpAddress(ip_end)){
                        ipNums=countIp(ip_begin,ip_end);
                        if(ipNums>0&&ipNums<=256){
                            db.execSQL("insert into group_info values(null,?,?,?,?,?)", new String[]{group_name, group_desc, ip_begin, ip_end,ipNums+""});
                            Toast.makeText(add_group.this, "添加分组成功！", Toast.LENGTH_SHORT).show();
                            newGroupToReturn();
                            add_group.this.finish();
                        }
                        else if(ipNums==-1)
                            Toast.makeText(add_group.this, "请输入有效ip地址！", Toast.LENGTH_SHORT).show();
                        else{
                            AlertDialog tooMuchIp=new AlertDialog.Builder(this).create();
                            tooMuchIp.setTitle("系统提示：");
                            tooMuchIp.setMessage("当前ip范围包含" + ipNums + "个ip地址,导致系统响应时间过长，确定要添加吗？");
                            tooMuchIp.setButton("确定", listener1);
                            tooMuchIp.setButton2("取消", listener1);
                            tooMuchIp.show();
                        }
//                            Toast.makeText(add_group.this, "系统提示:地址无效!请重新输入!\n当前ip范围包含 "+ipNums+" 个ip地址\n导致系统响应时间过长", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(add_group.this, "结束地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(add_group.this, "起始地址无效，请重新输入！", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private boolean checkInput(){//检查是否输入完整
        if(TextUtils.isEmpty(et_group_name.getText().toString())){
            Toast.makeText(add_group.this, "请输入分组名称！", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(TextUtils.isEmpty(et_ip_begin.getText().toString())){
            Toast.makeText(add_group.this, "请输入开始地址！", Toast.LENGTH_SHORT).show();
            return false;
        }

        else if(TextUtils.isEmpty(et_ip_end.getText().toString())){
            Toast.makeText(add_group.this, "请输入结束地址！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean isValidCIpAddress(String ip){
        if(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
            String[] strs=ip.split("\\.");
            for(int i=0;i<4;i++)
                for(int j=0;j<strs[i].length();j++){
                    char c=strs[i].charAt(j);
                    if (!(c>='0'&&c<='9'))
                        return false;
                }
            if(Integer.parseInt(strs[0])>=1&&Integer.parseInt(strs[0])<=223)
                if(Integer.parseInt(strs[1])<=255)
                    if(Integer.parseInt(strs[2])<=255)
                        if(Integer.parseInt(strs[3])<=255)
                            return true;
        }
        return false;
    }
    public int countIp(String fromIp,String toIp){//计算从fromIp到toIp包含的ip个数
        String[] begin=fromIp.split("\\.");
        String[] end=toIp.split("\\.");
        if(begin.length!=4||end.length!=4||fromIp.charAt(0)=='.'||toIp.charAt(0)=='.')
            return -1;
        int count=1;
        int tmp=256*256*256;
        for(int i=0;i<4;i++){
            count+=tmp*(Integer.parseInt(end[i])-Integer.parseInt(begin[i]));
            tmp=tmp/256;
        }
        return count;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog isExit=new AlertDialog.Builder(this).create();
            isExit.setTitle("系统提示：");
            isExit.setMessage("添加分组未完成，确定要退出吗？");
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            isExit.show();
        }
        return false;
    }
    //处理在添加分组界面出现的点击系统返回键直接退回主屏幕的问题，监听系统返回键
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    Intent intent=getIntent();
                    add_group.this.setResult(1, intent);
                    Toast.makeText(add_group.this, "添加分组失败！", Toast.LENGTH_SHORT).show();
                    add_group.this.finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
    //处理所选ip范围过大时，在添加界面给出的提示对话框。
    DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    String group_name = et_group_name.getText().toString();
                    String group_desc = et_group_desc.getText().toString();
                    String ip_begin = et_ip_begin.getText().toString();
                    String ip_end = et_ip_end.getText().toString();
                    dbHelper.getWritableDatabase().execSQL("insert into group_info values(null,?,?,?,?,?)", new String[]{group_name, group_desc, ip_begin, ip_end,ipNums+""});
                    Toast.makeText(add_group.this, "添加分组成功！", Toast.LENGTH_SHORT).show();
                    newGroupToReturn();
                    add_group.this.finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
}
