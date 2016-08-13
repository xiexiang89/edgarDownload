package com.edgar.download;

/**
 * Created by Edgar on 2016/4/19.
 * 下载请求对象
 */
public class DownloadRequest {
    private String downloadUrl;
    private String saveFilePath;
    private String contentType;
    private String name;
    private long totalSize;
    private long currentSize;
    private int progress;
    private int status;
    private int failType;
    private String failMessage;
    private int maxRetryCount;
    private int timeoutMs;

    public DownloadRequest(){
        setMaxRetryCount(DownloadConstant.DEFAULT_MAX_RETRIES);
        setTimeoutMs(DownloadConstant.DEFAULT_TIMEOUT_MS);
    }

    /**
     * Get download url
     * @return
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Set download url
     * @param downloadUrl
     */
    public DownloadRequest setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    /**
     * Get Save file path
     * @return
     */
    public String getSaveFilePath() {
        return saveFilePath;
    }

    /**
     * Set Save file path
     * @param saveFilePath
     */
    public DownloadRequest setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public DownloadRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getName() {
        return name;
    }

    public DownloadRequest setName(String name) {
        this.name = name;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public DownloadRequest setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public DownloadRequest setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public DownloadRequest setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public DownloadRequest setStatus(int status) {
        this.status = status;
        return this;
    }

    public int getFailType() {
        return failType;
    }

    public DownloadRequest setFailType(int failType) {
        this.failType = failType;
        return this;
    }

    public DownloadRequest setFailMessage(String failMessage) {
        this.failMessage = failMessage;
        return this;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}