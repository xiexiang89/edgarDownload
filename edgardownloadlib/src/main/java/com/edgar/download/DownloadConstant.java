package com.edgar.download;

/**
 * Created by Edgar on 2016/7/4.
 */
public final class DownloadConstant {
    /** The default socket timeout in milliseconds */
    public static final int DEFAULT_TIMEOUT_MS = 5000;

    /** The default number of retries */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /** The default backoff multiplier */
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public static final int DEFAULT_BUFFER_SIZE = 8*1024;
}