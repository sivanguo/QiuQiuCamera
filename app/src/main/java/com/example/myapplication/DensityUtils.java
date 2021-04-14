package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtils {

    private static final String TAG=DensityUtils.class.getSimpleName();

    public static int dip2px(Context context,float dipValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context,float pxValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static DisplayMetrics getScreenSize(Activity context){
        DisplayMetrics dm=new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
