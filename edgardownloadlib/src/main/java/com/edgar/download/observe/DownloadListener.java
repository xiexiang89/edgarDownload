package com.edgar.download.observe;

import com.edgar.download.DownloadRequest;
import com.edgar.download.task.AbsDownloadTask;

import java.io.File;

/**
 * Created by edgar on 2016/1/29.
 * 下载监听
 */
public interface DownloadListener {
    /**
     * 线程运行调用
     * @param downloadTask 下载任务
     */
    void onRunning(AbsDownloadTask downloadTask);
    /**
     * 开始传输文件会调用
     * @param downloadTask 下载任务
     */
    void onStart(AbsDownloadTask downloadTask);
    /**
     * 等待下载
     */
    void onWait(AbsDownloadTask downloadTask, String url);
    /**
     * 暂停下载
     * @param downloadTask 下载任务
     * @param url 下载地址
     */
    void onPause(AbsDownloadTask downloadTask, String url);
    /**
     * 下载完成
     * @param downloadTask 下载任务
     * @param url 下载地址
     * @param filePath 下载保存路径
     */
    void onFinish(AbsDownloadTask downloadTask, String url, File filePath);
    /**
     * 失败
     * @param downloadTask 下载任务对象
     * @param url 下载url
     * @param errorCode 失败码
     * @param errorMsg 失败消息
     */
    void onFail(AbsDownloadTask downloadTask, String url, int errorCode, String errorMsg);
    /**
     * 取消会调用
     * @param downloadTask 下载任务
     * @param url 下载地址
     */
    void onCancel(AbsDownloadTask downloadTask, String url);
    /**
     * 进度更新
     * @param downloadTask 下载任务
     * @param totalSize 文件总大小
     * @param currentSize  已经下载大小
     * @param progress 计算好的进度
     */
    void onUpdateProgress(AbsDownloadTask downloadTask, long totalSize,long currentSize,int progress);
}