package com.edgar.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.edgar.download.observe.NetworkObserve;
import com.edgar.download.task.AbsDownloadTask;
import com.edgar.download.task.HttpDownloadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Edgar on 2016/4/20.
 */
public class DownloadManager {

    private static final int MAX_QUEUE_SIZE = 1;   //Max download queue is 2.
    private static final int MSG_NEXT_TASK = 0;
    private final List<AbsDownloadTask> mAllDownloadList = Collections.synchronizedList(new ArrayList<AbsDownloadTask>());
    private final List<AbsDownloadTask> mPauseList = Collections.synchronizedList(new ArrayList<AbsDownloadTask>());
    private final List<AbsDownloadTask> mFinishList = Collections.synchronizedList(new ArrayList<AbsDownloadTask>());
    private final ConcurrentLinkedQueue<AbsDownloadTask> mWaitDownloadList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<AbsDownloadTask> mDownloadingList = new ConcurrentLinkedQueue<>();

    private static volatile DownloadManager sInstance;
    private Context mAppContext;
    private BroadcastReceiver mNetworkMonitor;
    private TaskCreateFactory mTaskCreateFactory;
    private ThreadPoolExecutor mDownloadExecutor;
    private int mMaxQueueSize = MAX_QUEUE_SIZE;
    private final Handler mNextTaskHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_NEXT_TASK:
                    if (!mWaitDownloadList.isEmpty()){
                        AbsDownloadTask absDownloadTask = mWaitDownloadList.poll();
                        if (isTaskWait(absDownloadTask)){
                            executorDownload(absDownloadTask);
                        }
                    }
                    break;
            }
        }
    };

    private DownloadManager(Context context){
        mAppContext = context.getApplicationContext();
        mDownloadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(mMaxQueueSize);
        registerNetworkReceiver();
    }

    public static DownloadManager getInstance(Context context){
        if (sInstance == null){
            synchronized (DownloadManager.class){
                if (sInstance == null){
                    sInstance = new DownloadManager(context);
                }
            }
        }
        return sInstance;
    }

    private void registerNetworkReceiver(){
        mNetworkMonitor = new NetworkObserve(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mAppContext.registerReceiver(mNetworkMonitor,intentFilter);
    }

    private void unregisterNetworkReceiver(){
        mAppContext.unregisterReceiver(mNetworkMonitor);
    }

    public AbsDownloadTask addDownloadTask(DownloadRequest downloadRequest){
        if (downloadRequest == null) return null;
        if (mTaskCreateFactory == null){
            mTaskCreateFactory = new TaskCreateFactory() {
                @Override
                public AbsDownloadTask create(DownloadRequest downloadRequest) {
                    return new HttpDownloadTask(mAppContext,downloadRequest);
                }
            };
        }
        AbsDownloadTask downloadTask = mTaskCreateFactory.create(downloadRequest);
        downloadTask.setDownloadQueueManager(this);
        executorDownload(downloadTask);
        return downloadTask;
    }

    private void executorDownload(AbsDownloadTask downloadTask){
        if (mDownloadingList.contains(downloadTask)){
            return;
        }
        if(isTaskFinish(downloadTask)) return;
        if(mPauseList.contains(downloadTask)){
            mPauseList.remove(downloadTask);
        }
        if (!mAllDownloadList.contains(downloadTask)) {
            mAllDownloadList.add(downloadTask);
        }
        if(mDownloadingList.size() < MAX_QUEUE_SIZE){
            if (!mDownloadingList.contains(downloadTask)){
                mDownloadingList.add(downloadTask);
                downloadTask.startDownload(mDownloadExecutor);
            }
        } else {
            if (!mWaitDownloadList.contains(downloadTask)){
                downloadTask.waitDownload();
                mWaitDownloadList.add(downloadTask);
            }
        }
    }

    public void resumeDownloadTask(AbsDownloadTask downloadTask){
        executorDownload(downloadTask);
    }

    public void removeDownload(AbsDownloadTask downloadTask){
        if(mAllDownloadList.contains(downloadTask)){
            mAllDownloadList.remove(downloadTask);
        }
        if(mPauseList.contains(downloadTask)){
            mPauseList.remove(downloadTask);
        }
        if (mWaitDownloadList.contains(downloadTask)){
            mWaitDownloadList.remove(downloadTask);
        }
        if (mDownloadingList.contains(downloadTask)){
            mDownloadingList.remove(downloadTask);
        }
        downloadTask.cancelDownload();
    }

    public void pauseDownload(AbsDownloadTask downloadTask){
        if(isTaskFinish(downloadTask)){
            return;
        }
        mWaitDownloadList.remove(downloadTask);
        mDownloadingList.remove(downloadTask);
        downloadTask.pauseDownload();
        mPauseList.add(downloadTask);
    }

    public void pauseAllDownload(){
        Iterator<AbsDownloadTask> iterator = mWaitDownloadList.iterator();
        while (iterator.hasNext()){
            pauseDownload(iterator.next());
        }
        iterator = mDownloadingList.iterator();
        while (iterator.hasNext()){
            pauseDownload(iterator.next());
        }
    }

    private boolean isTaskWait(AbsDownloadTask downloadTask){
        return downloadTask != null && downloadTask.isWait();
    }

    private boolean isTaskFinish(AbsDownloadTask downloadTask){
        boolean isTaskFinish = downloadTask.isFinish() && downloadTask.getFile().exists();
        if (!isTaskFinish){
            if (mFinishList.contains(downloadTask)){
                mFinishList.remove(downloadTask);
            }
        }
        return isTaskFinish;
    }

    public void finishTask(AbsDownloadTask downloadTask){
        if(!mFinishList.contains(downloadTask)){
            mFinishList.add(downloadTask);
        }
    }

    /**
     * Go to next task.
     * @param downloadTask
     */
    public void nextTask(AbsDownloadTask downloadTask){
        mDownloadingList.remove(downloadTask);
        mNextTaskHandler.sendEmptyMessage(MSG_NEXT_TASK);
    }

    public void configTaskCreateFactory(TaskCreateFactory taskCreateFactory) {
        mTaskCreateFactory = taskCreateFactory;
    }

    public void release(){
        unregisterNetworkReceiver();
        pauseAllDownload();
        mAllDownloadList.clear();
        mFinishList.clear();
        mPauseList.clear();
        mDownloadingList.clear();
        mWaitDownloadList.clear();
        mDownloadExecutor.shutdown();
    }
}