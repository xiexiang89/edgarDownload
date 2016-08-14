package com.edgar.download.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.edgar.download.DownloadLog;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by Edgar on 2016/7/5.
 */
public final class Utils {
    private Utils(){}

    /**
     * Check phone network whether connection
     * @param context Current use context
     * @return If network connected return true.
     */
    public static boolean isNetworkConnected(Context context){
        if (context != null){
            ConnectivityManager connectivityManager = getConnectivityManager(context);
            NetworkInfo mNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null){
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * Check Phone current WIFI network whether connection.
     * @param context Current use context
     * @return If wifi network connected return true.
     */
    public static boolean isWifiConnected(Context context){
        if (context != null){
            ConnectivityManager connectivityManager = getConnectivityManager(context);
            NetworkInfo mWiFiNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null){
                return mWiFiNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    /**
     * Check phone mobile network whether connected.
     * @param context The current use context
     * @return Phone mobile network no connected.
     */
    public static boolean isMobileConnected(Context context){
        if (context != null){
            ConnectivityManager connectivityManager = getConnectivityManager(context);
            NetworkInfo mMobileNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null){
                return mMobileNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    /**
     * Get phone current network type.
     * @param context The current use context
     * @return Current network connect type.
     */
    public static int getConnectedType(Context context){
        if (context != null){
            ConnectivityManager connectivityManager = getConnectivityManager(context);
            NetworkInfo mNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()){
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * Get Connectivity manager system server/
     * @param context Current use context
     * @param <T> generic paradigm
     * @return return ConnectivityManager object
     */
    @SuppressLint("ServiceCast")
    public static <T> T getConnectivityManager(Context context){
        return (T) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static void closeQuietly(Closeable closeable){
        try{
            if(closeable != null){
                closeable.close();
            }
        } catch (IOException e){
            DownloadLog.e(e.getMessage());
        }
    }

    /**
     * Get current use version
     * @see Build.VERSION#SDK_INT
     * @return return SDK_INT
     */
    public static int getVersionCode(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * Check current version whether Android 2.3
     * @see Build.VERSION_CODES#GINGERBREAD
     * @return If current version whether android2.3 return true.
     */
    public static boolean hasGingerbread() {
        return getVersionCode() >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static long strToLong(String value,long defValue){
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e){
            return defValue;
        }
    }

    public static int getProgress(long downloadSize,long totalSize){
        return (int) ((float) downloadSize / totalSize * 100);
    }
}