package com.circloop.deviceManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by 浩思于微 on 2016/6/19.
 */
public class groupLinearLayout extends LinearLayout {
    public groupLinearLayout(Context context) {
        super(context);
    }

    public groupLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public groupLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        System.out.println("进入onInterceptTouchEvent");
//        boolean flag=false;
//        switch (ev.getAction()){
//            case MotionEvent.ACTION_UP:
//                int xDiff= (int) Math.abs(ev.getX()-mLastPosition);
//                System.out.println("mLastPosition"+mLastPosition);
//                System.out.println("xDiff"+xDiff);
//                if(xDiff<5)
//                    flag=true;
//                break;
//            case MotionEvent.ACTION_DOWN:
//                mLastPosition=ev.getX();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//
//        }
//        System.out.println(flag);
//        return flag;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        System.out.println("进图了ontouch");
//        return true;
//    }
}
