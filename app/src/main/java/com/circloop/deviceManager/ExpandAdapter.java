package com.circloop.deviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.circloop.database.Child;
import com.circloop.database.Group;

public class ExpandAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private LayoutInflater mInflater = null;
    private List<List<Child>>   mData = null;
    private List<Group> groupList;
    private static int indexOfNewGroup=-1;
    private  List<SlideDelete> slideDeleteArrayList = new ArrayList<SlideDelete>();

    public ExpandAdapter(Context ctx,List<Group> groupList,List<List<Child>> list) {
        mContext = ctx;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
        this.groupList=groupList;
    }
    public static void setIndexOfNewGroup(int index){
        indexOfNewGroup=index;
    }
    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition).size();
    }

    @Override
    public List<Child> getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition);
    }

    @Override
    public Child getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition,  final boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_item_layout, null);
        }
        GroupViewHolder holder = new GroupViewHolder();
        holder.mGroupName = (TextView) convertView
                .findViewById(R.id.group_name);
        holder.mGroupName.setText(groupList.get(groupPosition).getGroupName());
        holder.mGroupCount = (TextView) convertView
                .findViewById(R.id.group_count);
        holder.mGroupCount.setText(" [" + mData.get(groupPosition).size() + "]");
        holder.progressBar= (ProgressBar) convertView.findViewById(R.id.progressBar);
        holder.progressText= (TextView) convertView.findViewById(R.id.progressText);
        holder.mElvContent= (LinearLayout) convertView.findViewById(R.id.mElvContent);
        holder.mEdit= (LinearLayout) convertView.findViewById(R.id.edit_llayout);
        holder.mDelete= (LinearLayout) convertView.findViewById(R.id.del_llayout);
        holder.mSlideDelete= (SlideDelete) convertView.findViewById(R.id.mSlideDelete);
        holder.mElvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded)
                    HomeActivity.mListView.collapseGroup(groupPosition);
                else if(slideDeleteArrayList.size()==0)
                    HomeActivity.mListView.expandGroup(groupPosition,true);
                else
                    closeOtherItem();
            }
        });
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContext,"您点击了编辑按钮"+groupList.get(groupPosition).getGroupName(),Toast.LENGTH_SHORT).show();
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(groupPosition);
//                Toast.makeText(mContext,"您点击了删除按钮",Toast.LENGTH_SHORT).show();
            }
        });
        holder.mSlideDelete.setOnSlideDeleteListener(new SlideDelete.OnSlideDeleteListener() {//接口回调，为了避免同时出现多个item的删除视图的问题
            @Override
            public void onOpen(SlideDelete slideDelete) {
//               System.out.println("onOPEN..................................");
                closeOtherItem();
                slideDeleteArrayList.add(slideDelete);
            }

            @Override
            public void onClose(SlideDelete slideDelete) {
//               System.out.println("onClose......................................");
                slideDeleteArrayList.remove(slideDelete);
            }
        });

        if(groupList.get(groupPosition).isDiscover()){
            Group group=groupList.get(groupPosition);
            int max=group.progressBarMax;
            if(group.isSnmpDis() && max==group.getProgress()){
                group.setIsDiscover(false);
                holder.progressText.setVisibility(View.INVISIBLE);
                holder.progressBar.setVisibility(View.INVISIBLE);
            }
            else{
                holder.progressText.setVisibility(View.VISIBLE);
                if(!group.isSnmpDis())
                    holder.progressText.setText("正在ping设备："+group.getProgress()+"/"+group.progressBarMax);
                else
                    holder.progressText.setText("正在识别设备类型："+group.getProgress()+"/"+group.progressBarMax);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(group.getProgress());
                holder.progressBar.setMax(max);
            }
        }
        else {
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.progressText.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
    private void closeOtherItem(){
//        System.out.println("closeOtherItem");
        // 采用Iterator的原因是for是线程不安全的，迭代器是线程安全的
        ListIterator<SlideDelete> slideDeleteListIterator = slideDeleteArrayList.listIterator();
        while(slideDeleteListIterator.hasNext()){
            SlideDelete slideDelete = slideDeleteListIterator.next();
            slideDelete.isShowDelete(false);
        }
        slideDeleteArrayList.clear();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.child_item_layout, null);
        }
        ChildViewHolder holder = new ChildViewHolder();
        holder.mIcon = (ImageView) convertView.findViewById(R.id.img);
        holder.mIcon.setBackgroundResource(R.drawable.b);
        holder.mChildName = (TextView) convertView.findViewById(R.id.item_name);
        holder.mChildName.setText(getChild(groupPosition, childPosition).getDeviceType());
        holder.mDetail = (TextView) convertView.findViewById(R.id.item_detail);
        holder.mDetail.setText(getChild(groupPosition, childPosition).getIp());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        /*很重要：实现ChildView点击事件，必须返回true*/
        return true;
    }
    //侧滑之后点击删除按钮，根据groupPosition来删除分组，并且更新数据库
    public void removeItem(int position) {
        long pos = HomeActivity.mListView.getExpandableListPosition(position);
//        int childPos = ExpandableListView.getPackedPositionChild(pos);// 获取child的id
        int groupPos = ExpandableListView.getPackedPositionGroup(pos);// 获取group的id
        String group_name_to_del=groupList.get(groupPos).getGroupName();
        groupList.remove(groupPos);
        mData.remove(groupPos);
        HomeActivity.getAdapter().notifyDataSetChanged();
//        String sql_delete_group_item="delete from group_info where _id="+(groupPos+1);//不能直接根据id来删除，因为删除之后的空缺是不会自动补上的，这样如果删除了之前的元素
        //再往后删除就会出问题
        String sql_delete_group_item="delete from group_info where group_name='"+group_name_to_del+"'";
        HomeActivity.dbHelper.getWritableDatabase().execSQL(sql_delete_group_item);
    }


    private class GroupViewHolder {
        TextView mGroupName;
        TextView mGroupCount;
        ProgressBar progressBar;
        TextView progressText;
        LinearLayout mElvContent;
        LinearLayout mEdit;
        LinearLayout mDelete;
        SlideDelete mSlideDelete;
    }
    private class ChildViewHolder {
        ImageView mIcon;
        TextView mChildName;
        TextView mDetail;
    }

}
