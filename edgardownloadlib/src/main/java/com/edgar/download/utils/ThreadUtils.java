package com.edgar.download.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Edgar on 2016/7/5.
 */
public final class ThreadUtils {
    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    private ThreadUtils(){}

    public static void postMainThread(Runnable runnable){
        if (runnable == null) return;
        if (Looper.getMainLooper() == Looper.myLooper()){
            runnable.run();
        }  else {
            sMainHandler.post(runnable);
        }
    }
}