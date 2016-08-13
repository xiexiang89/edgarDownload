package com.edgar.download.observe;

import com.qida.download.DownloadRequest;

import java.io.File;

/**
 * Created by edgar on 2016/3/26.
 * 默认的监听实现.可以不用实现多个方法,只需实现onDownloading方法就行
 */
public class DefaultDownloadListener implements DownloadListener {

    @Override
    public void onRunning(DownloadRequest downloadRequest) {}

    @Override
    public void onStart(DownloadRequest downloadRequest) {}

    @Override
    public void onWait(DownloadRequest downloadRequest, String url) {}

    @Override
    public void onPause(DownloadRequest downloadRequest, String url, int progress) {}

    @Override
    public void onFinish(DownloadRequest downloadRequest, String url, File filePath) {}

    @Override
    public void onFail(DownloadRequest downloadRequest, String url, int errorCode, String errorMsg) {}

    @Override
    public void onCancel(DownloadRequest downloadRequest, String url) {}

    @Override
    public void onUpdateProgress(DownloadRequest downloadRequest, String url, int progress) {}
}