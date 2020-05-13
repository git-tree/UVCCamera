package com.serenegiant.usbcameratest0.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {
    //toast
    public static void showToast(Context activity, String str, int showTime)
    {
        Toast toast = Toast.makeText(activity, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.rgb(30,144,255));     //设置字体颜色,道奇蓝
        toast.show();
    }
    public static void showToast(Context activity, String str, int showTime,int color)
    {
        Toast toast = Toast.makeText(activity, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(color);     //设置字体颜色,道奇蓝
        toast.show();
    }
}
