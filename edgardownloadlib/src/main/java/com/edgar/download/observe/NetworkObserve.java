package com.edgar.download.observe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.edgar.download.DownloadLog;
import com.edgar.download.DownloadQueueManager;
import com.edgar.download.utils.Utils;

/**
 * Created by Edgar on 2016/7/5.
 * Network change observe.
 */
public final class NetworkObserve extends BroadcastReceiver {

    private DownloadQueueManager mDownloadQueueManager;

    public NetworkObserve(DownloadQueueManager downloadQueueManager){
        mDownloadQueueManager = downloadQueueManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            final int networkType = Utils.getConnectedType(context);
            if (networkType == ConnectivityManager.TYPE_MOBILE){
                DownloadLog.e("Current network type is mobile:"+networkType);
                mDownloadQueueManager.pauseAllDownload();
            }
        }
    }
}