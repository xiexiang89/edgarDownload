package com.edgar.download;

/**
 * Created by Edgar on 2016/8/3.
 * 错误类型
 */
public class ErrorType {

    //失败类型的定义
    /**
     * 网络异常
     */
    public final static int NETWORK_ERROR = 0x1;
    /**
     * 服务器异常
     */
    public final static int SERVER_ERROR = 0x2;
    /**
     * 网络错误
     */
    public final static int NOT_CONTENT = 0x3;
    /**
     * Io异常
     */
    public final static int IO_ERROR = 0x4;
    /**
     * 空间不足
     */
    public final static int NOT_AVAILABLE_SPACE = 0x5;
    /**
     * SD卡不可用
     */
    public final static int SD_NOT_AVAILABLE = 0x6;
    public final static int URL_ERROR = 0x7;
    /**
     * 未知错误
     */
    public final static int UNKNOWN_ERROR = -1;
}
