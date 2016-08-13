package com.edgar.download.observe;

import com.edgar.download.DownloadRequest;

import java.io.File;

/**
 * Created by edgar on 2016/1/29.
 * 下载监听
 */
public interface DownloadListener {
    /**
     * 线程运行调用
     * @param downloadRequest
     */
    void onRunning(DownloadRequest downloadRequest);
    /**
     * 开始传输文件会调用
     * @param downloadRequest
     */
    void onStart(DownloadRequest downloadRequest);
    /**
     * 等待下载
     * @param downloadRequest
     */
    void onWait(DownloadRequest downloadRequest, String url);
    /**
     * 暂停下载
     * @param downloadRequest
     * @param url
     * @param progress
     */
    void onPause(DownloadRequest downloadRequest, String url, int progress);
    /**
     * 下载完成
     * @param downloadRequest
     * @param url
     * @param filePath
     */
    void onFinish(DownloadRequest downloadRequest, String url, File filePath);
    /**
     * 失败
     * @param downloadRequest 下载任务对象
     * @param url 下载url
     * @param errorCode 失败码
     * @param errorMsg 失败消息
     */
    void onFail(DownloadRequest downloadRequest, String url, int errorCode, String errorMsg);
    /**
     * 取消会调用
     * @param downloadRequest
     * @param url
     */
    void onCancel(DownloadRequest downloadRequest, String url);
    /**
     * 进度更新
     * @param downloadRequest
     */
    void onUpdateProgress(DownloadRequest downloadRequest, String url, int progress);
}