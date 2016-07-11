package com.circloop.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 浩思于微 on 2016/5/12.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_GROUPINFO_SQL ="create table group_info(_id integer primary key " +
            "autoincrement,group_name,group_desc,ip_begin,ip_end,ip_nums)";
    final String CREATE_TABLE_DEVICEINFO_SQL ="create table device_info(_id integer primary key autoincrement,device_type,ip,oid)";
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_GROUPINFO_SQL);
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICEINFO_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
