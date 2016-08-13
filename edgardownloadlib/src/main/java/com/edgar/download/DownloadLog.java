package com.edgar.download;

import android.util.Log;

/**
 * Created by edgar on 2016/3/26.
 */
public class DownloadLog {

    public static final String TAG = "QiDa_Download";

    public static void e(String msg){
        Log.e(TAG,msg);
    }

    public static void e(String msg,Throwable e){
        Log.e(TAG,msg,e);
    }

    public static void i(String msg){
        Log.i(TAG,msg);
    }

    public static void i(String msg,Throwable e){
        Log.i(TAG,msg,e);
    }

    public static void w(String msg){
        Log.w(TAG,msg);
    }

    public static void w(String msg,Throwable e){
        Log.i(TAG,msg,e);
    }

    public static void v(String msg){
        Log.e(TAG,msg);
    }

    public static void v(String msg,Throwable e){
        Log.i(TAG,msg,e);
    }

    public static void d(String msg){
        Log.e(TAG,msg);
    }

    public static void d(String msg,Throwable e){
        Log.i(TAG,msg,e);
    }

}