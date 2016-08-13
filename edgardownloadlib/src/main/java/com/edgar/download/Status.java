package com.edgar.download;

/**
 * Created by Edgar on 2016/4/18.
 * 下载状态码定义
 */
public class Status {
    public final static int START = 0;
    /**
     * 下载进行中
     */
    public final static int DOWNLOADING = 1;
    /**
     * 暂停
     */
    public final static int PAUSE = 2;
    /**
     * 等待下载
     */
    public final static int WAIT = 3;
    /**
     * 已取消下载
     */
    public final static int CANCEL = 4;
    /**
     * 下载失败
     */
    public final static int FAIL = 5;
    /**
     * 下载完成
     */
    public final static int FINISHED = 6;
}