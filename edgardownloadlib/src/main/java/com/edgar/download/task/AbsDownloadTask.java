package com.edgar.download.task;

import android.content.Context;

import com.edgar.download.DownloadLog;
import com.edgar.download.DownloadManager;
import com.edgar.download.DownloadRequest;
import com.edgar.download.ErrorType;
import com.edgar.download.Status;
import com.edgar.download.R;
import com.edgar.download.exception.DownloadErrorException;
import com.edgar.download.exception.PauseException;
import com.edgar.download.exception.StopException;
import com.edgar.download.observe.DownloadListener;
import com.edgar.download.utils.StorageUtils;
import com.edgar.download.utils.ThreadUtils;
import com.edgar.download.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;

/**
 * Created by edgar on 2016/3/27.
 * 抽象的下载任务
 */
public abstract class AbsDownloadTask implements Runnable{
    private static final Object LOCK = new Object();
    private DownloadManager mDownloadManager;
    protected final DownloadTaskInfo mDownloadTaskInfo;
    protected DownloadListener mDownloadListener;
    private Context mContext;

    protected class DownloadTaskInfo{
        String downloadUrl;
        File saveFilePath;
        long totalSize;
        long currentSize;
        int progress;
        int status;
        int failType;
        String failMessage;
        int maxRetryCount;
        int timeoutMs;

        public DownloadTaskInfo(DownloadRequest downloadRequest){
            downloadUrl = downloadRequest.getDownloadUrl();
            saveFilePath = new File(downloadRequest.getSaveFilePath());
            totalSize = downloadRequest.getTotalSize();
            currentSize = downloadRequest.getCurrentSize();
            status = downloadRequest.getStatus();
            maxRetryCount = downloadRequest.getMaxRetryCount();
            timeoutMs = downloadRequest.getTimeoutMs();
        }

        public void changeStatus(int status){
            this.status = status;
        }
    }

    public AbsDownloadTask(Context context, DownloadRequest downloadRequest){
        mContext = context;
        mDownloadTaskInfo = new DownloadTaskInfo(downloadRequest);
    }

    @Override
    public void run() {
        try {
            notifyRunning();
            //1.Check network status;
            //2.Check storage status;
            //3.Check pause and stop status.
            checkNetworkConnectivity();
            checkStorageState();
            checkUserPauseOrStop();
            createParentFile();
            checkLocalFileValid();
            mDownloadTaskInfo.changeStatus(Status.DOWNLOADING);
            onDownloading();
            executeDownload();
            //Download finish.
            finishDownload();
        } catch (DownloadErrorException e){
            DownloadLog.e(e.getMessage());
            mDownloadTaskInfo.changeStatus(Status.FAIL);
            onFailDownload();
            notifyFail();
        } catch (StopException e){
            notifyCancel();
        } catch (PauseException e){
            notifyPause();
        } finally {
            nextTask();
        }
    }

    protected abstract void executeDownload()throws DownloadErrorException, StopException, PauseException;
    public void startDownload(ExecutorService executorService){
        mDownloadTaskInfo.changeStatus(Status.START);
        onStartDownload();
        executorService.execute(this);
    }

    public void waitDownload(){
        mDownloadTaskInfo.changeStatus(Status.WAIT);
        onWaitDownload();
        notifyWait();
    }

    /**
     * 开始下载调用
     */
    protected void onStartDownload(){}

    /**
     * 正在下载调用
     * @see Status#DOWNLOADING
     */
    protected void onDownloading(){}
    /**
     * 暂停下载调用
     */
    protected void onPauseDownload(){}
    /**
     * 取消下载调用
     */
    protected void onCancelDownload(){}
    /**
     * 完成下载
     */
    protected void onFinishDownload(){}
    /**
     * 任务等待时候会调用
     */
    protected void onWaitDownload(){}

    /**
     * 下载失败时候会调用
     */
    protected void onFailDownload(){}

    /**
     * 执行完成,不论下载失败成功都会调用
     */
    protected void onNextTask(){}
    private void nextTask(){
        DownloadLog.e(String.format("Download status:%s",getStatus()));
        onNextTask();
        if (mDownloadManager != null){
            mDownloadManager.nextTask(this);
        }
    }

    /**
     * 进度更新时调用
     * @param totalSize 总大小
     * @param currentSize 当前写的大小
     * @param progress 进度
     */
    protected void onUpdateProgress(long totalSize,long currentSize,int progress){}

    private void checkLocalFileValid(){
        if (getCurrentSize() <= 0){
            //下载大小为0,删除文件.
            if (mDownloadTaskInfo.saveFilePath.delete()){
                DownloadLog.e("Delete old download file success:"+mDownloadTaskInfo.saveFilePath);
            }
        } else if (!mDownloadTaskInfo.saveFilePath.exists()){
            //文件不存在,重置下载大小为0.
            mDownloadTaskInfo.currentSize = 0;
        }
    }

    private void checkNetworkConnectivity()throws PauseException {
        if(!Utils.isNetworkConnected(mContext)){
            pauseDownload();
            DownloadLog.e("Network not connection,download file fail:"+getDownloadUrl());
            throw new PauseException();
        }
    }

    /**
     * Check storage state is remove.
     * @throws DownloadErrorException
     */
    protected void checkStorageState()throws DownloadErrorException {
        if(StorageUtils.isExternalStorageRemovable()){
            handlerDownloadFail(ErrorType.SD_NOT_AVAILABLE,mContext.getString(R.string.sd_not_available));
            throwDownloadErrorException("Storage remove.");
        }
    }

    protected void checkStorageNotEnough(long length) throws DownloadErrorException{
        if(!StorageUtils.isAvailable(length)){
            handlerDownloadFail(ErrorType.NOT_AVAILABLE_SPACE,mContext.getString(R.string.download_not_space));
            throwDownloadErrorException("Storage not available space:"+length);
        }
    }

    /**
     * 检查用户是否触发暂停或取消.
     * @throws PauseException
     * @throws StopException
     */
    protected void checkUserPauseOrStop()throws PauseException,StopException{
        if(isPauseDownload()){
            throw new PauseException();
        }
        if(isCancelDownload()){
            throw new StopException();
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void pauseDownload() {
        final int oldStatus = mDownloadTaskInfo.status;
        mDownloadTaskInfo.changeStatus(Status.PAUSE);
        onPauseDownload();
        if (oldStatus == Status.WAIT){
            notifyPause();
        }
    }

    public void cancelDownload() {
        final int oldStatus = mDownloadTaskInfo.status;
        mDownloadTaskInfo.changeStatus(Status.CANCEL);
        onCancelDownload();
        if (oldStatus == Status.WAIT){
            notifyCancel();
        }
    }

    public boolean isCancelDownload() {
        return getStatus() == Status.CANCEL;
    }

    public boolean isPauseDownload() {
        return getStatus() == Status.PAUSE;
    }

    public boolean isError() {
        return getStatus() == Status.FAIL;
    }

    public int getErrorCode(){
        return mDownloadTaskInfo.failType;
    }

    public String getErrorMessage(){
        return mDownloadTaskInfo.failMessage;
    }

    public int getStatus() {
        return mDownloadTaskInfo.status;
    }

    public boolean isFinish(){
        return getStatus() == Status.FINISHED;
    }

    public boolean isWait(){
        return getStatus() == Status.WAIT;
    }

    public boolean isDownloading() {
        return getStatus() == Status.DOWNLOADING;
    }

    public String getDownloadUrl() {
        return mDownloadTaskInfo.downloadUrl;
    }

    public long getCurrentSize() {
        return mDownloadTaskInfo.currentSize;
    }

    public long getTotalSize() {
        return mDownloadTaskInfo.totalSize;
    }

    public int getProgress() {
        return mDownloadTaskInfo.progress;
    }

    public String getName() {
        return mDownloadTaskInfo.saveFilePath.getName();
    }

    public File getFile(){
        return mDownloadTaskInfo.saveFilePath;
    }

    public final int getMaxRetryCount(){
        return mDownloadTaskInfo.maxRetryCount;
    }

    public final int getTimeoutMs(){
        return mDownloadTaskInfo.timeoutMs;
    }

    public void setDownloadQueueManager(DownloadManager downloadManager){
        this.mDownloadManager = downloadManager;
    }

    /**
     * Set downloadListener
     * @param downloadListener 下载监听
     */
    public void setDownloadListener(DownloadListener downloadListener){
        mDownloadListener = downloadListener;
    }

    public DownloadListener getDownloadListener(){
        return mDownloadListener;
    }

    /**
     * If user pause the notify status.
     */
    void notifyPause(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onPause(AbsDownloadTask.this,getDownloadUrl());
                }
            }
        });
    }

    void notifyRunning(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onRunning(AbsDownloadTask.this);
                }
            }
        });
    }

    /**
     * If user stop the notify status
     */
    void notifyCancel(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onCancel(AbsDownloadTask.this,getDownloadUrl());
                }
            }
        });
    }

    void notifyProgress(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onUpdateProgress(AbsDownloadTask.this,getTotalSize(),getCurrentSize(),getProgress());
                }
            }
        });
    }

    protected void notifyWait(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onWait(AbsDownloadTask.this,getDownloadUrl());
                }
            }
        });
    }

    /**
     * Notify start download.
     */
    void notifyStart(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onStart(AbsDownloadTask.this);
                }
            }
        });
    }

    void notifyFail(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    int failType = getErrorCode();
                    mDownloadListener.onFail(AbsDownloadTask.this,getDownloadUrl(),failType,getErrorMessage());
                }
            }
        });
    }

    void notifyFinish(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onFinish(AbsDownloadTask.this,getDownloadUrl(),mDownloadTaskInfo.saveFilePath);
                }
            }
        });
    }

    private void postNotify(Runnable runnable){
        ThreadUtils.postMainThread(runnable);
    }

    /**
     * @see ErrorType#NETWORK_ERROR
     * @see ErrorType#NOT_CONTENT
     * @see ErrorType#SERVER_ERROR
     * @see ErrorType#UNKNOWN_ERROR
     * @see ErrorType#IO_ERROR
     * @see ErrorType#NOT_AVAILABLE_SPACE
     * @param failType 失败类型
     * @param failMessage 失败信息
     */
    protected void handlerDownloadFail(final int failType,final String failMessage){
        mDownloadTaskInfo.changeStatus(Status.FAIL);
        mDownloadTaskInfo.failType = failType;
        mDownloadTaskInfo.failMessage = failMessage;
    }

    protected void handlerNetworkFail(){
        handlerDownloadFail(ErrorType.NETWORK_ERROR,mContext.getString(R.string.download_network_error));

    }

    /**
     * 更新DownloadRequest
     * @param readLen 读取长度
     */
    private void updateDownloadRequest(long readLen){
        mDownloadTaskInfo.currentSize = readLen+getCurrentSize();
        final int downloadProgress = Utils.getProgress(getCurrentSize(),getTotalSize());
        mDownloadTaskInfo.progress = downloadProgress;
        onUpdateProgress(mDownloadTaskInfo.totalSize,mDownloadTaskInfo.currentSize,downloadProgress);
        notifyProgress();
    }

    protected void finishDownload(){
        if(mDownloadManager != null){
            mDownloadManager.finishTask(this);
        }
        mDownloadTaskInfo.changeStatus(Status.FINISHED);
        onFinishDownload();
        notifyFinish();
    }

    protected void throwDownloadErrorException(String message)throws DownloadErrorException{
        throw new DownloadErrorException(message);
    }

    protected void createParentFile()throws DownloadErrorException{
        synchronized (LOCK){
            File parentFile = mDownloadTaskInfo.saveFilePath.getParentFile();
            if(!parentFile.exists()){
                if(!parentFile.mkdirs()){
                    throw new DownloadErrorException(String.format("maker dir error:%s",parentFile.getAbsolutePath()));
                }
            }
        }
    }

    protected void writeToFile(RandomAccessFile randomAccessFile, byte[] buffers, int readLen)throws DownloadErrorException {
        try {
            //文件不存在,可能被用户在文件管理器中删除,这个时候没必要再次写,暂停下载.
            if(!isCancelDownload() && !mDownloadTaskInfo.saveFilePath.exists()){
                pauseDownload();
                return;
            }
            randomAccessFile.write(buffers,0,readLen);
            updateDownloadRequest(readLen);
        } catch (IOException e){
            handlerDownloadFail(ErrorType.IO_ERROR,mContext.getString(R.string.download_io_error));
            throwDownloadErrorException(String.format("Write file name:%s,error:%s",mDownloadTaskInfo.saveFilePath.getAbsolutePath(),e.getMessage()));
        }
    }

    protected RandomAccessFile createAndSeekFile(long position)throws DownloadErrorException{
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(mDownloadTaskInfo.saveFilePath,"rw");
            randomAccessFile.seek(position);
            return randomAccessFile;
        } catch (IOException e){
            handlerDownloadFail(ErrorType.IO_ERROR,mContext.getString(R.string.download_io_error));
            throw new DownloadErrorException(String.format("Create file fail:%s",e.getMessage()),e);
        }
    }

    protected int readBufferFromNetwork(InputStream inputStream, byte[] buffer)throws DownloadErrorException{
        try {
            return inputStream.read(buffer);
        } catch (IOException e){
            handlerNetworkFail();
            throw new DownloadErrorException(String.format("Read buffer fail:%s,errorMessage:%s",mDownloadTaskInfo.saveFilePath,e.getMessage()));
        }
    }
}