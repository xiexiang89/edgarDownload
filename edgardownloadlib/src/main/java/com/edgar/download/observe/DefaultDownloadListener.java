package com.edgar.download.observe;

import com.edgar.download.task.AbsDownloadTask;

import java.io.File;

/**
 * Created by edgar on 2016/3/26.
 * 默认的监听实现.可以不用实现多个方法,只需实现onDownloading方法就行
 */
public abstract class DefaultDownloadListener implements DownloadListener {

    @Override
    public void onRunning(AbsDownloadTask downloadTask) {

    }

    @Override
    public void onStart(AbsDownloadTask downloadTask) {

    }

    @Override
    public void onWait(AbsDownloadTask downloadTask, String url) {

    }

    @Override
    public void onPause(AbsDownloadTask downloadTask, String url) {

    }

    @Override
    public void onFinish(AbsDownloadTask downloadTask, String url, File filePath) {

    }

    @Override
    public void onFail(AbsDownloadTask downloadTask, String url, int errorCode, String errorMsg) {

    }

    @Override
    public void onCancel(AbsDownloadTask downloadTask, String url) {

    }
}