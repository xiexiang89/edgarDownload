package com.edgar.download;

/**
 * Created by Edgar on 2016/4/19.
 * 下载请求对象
 */
public class DownloadRequest {
    private final String downloadUrl;
    private final String saveFilePath;
    private final long totalSize;
    private final long currentSize;
    private final int status;
    private final int maxRetryCount;
    private final int timeoutMs;

    DownloadRequest(Builder builder){
        downloadUrl = builder.downloadUrl;
        saveFilePath = builder.saveFilePath;
        totalSize = builder.totalSize;
        currentSize = builder.currentSize;
        status = builder.status;
        maxRetryCount = builder.maxRetryCount;
        timeoutMs = builder.timeoutMs;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public int getStatus() {
        return status;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public static class Builder{
        private String downloadUrl;
        private String saveFilePath;
        private long totalSize;
        private long currentSize;
        private int status;
        private int maxRetryCount;
        private int timeoutMs;

        public Builder downloadUrl(String downloadUrl){
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder configSaveFilePath(String filePath){
            this.saveFilePath = filePath;
            return this;
        }

        public Builder setTotalSize(long totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        public Builder setCurrentSize(long currentSize) {
            this.currentSize = currentSize;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public Builder setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public DownloadRequest build(){
            if (maxRetryCount == 0){
                maxRetryCount = DownloadConstant.DEFAULT_MAX_RETRIES;
            }

            if (timeoutMs == 0){
                timeoutMs = DownloadConstant.DEFAULT_TIMEOUT_MS;
            }
            return new DownloadRequest(this);
        }
    }
}