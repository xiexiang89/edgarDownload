package com.edgar.download.task;

import android.content.Context;

import com.edgar.download.DownloadLog;
import com.edgar.download.DownloadQueueManager;
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
    private DownloadQueueManager mDownloadQueueManager;
    protected final DownloadRequest mDownloadRequest;
    protected DownloadListener mDownloadListener;
    private Context mContext;
    private File mSaveFile;

    public AbsDownloadTask(Context context, DownloadRequest downloadRequest){
        mContext = context;
        mDownloadRequest = downloadRequest;
        mSaveFile = new File(mDownloadRequest.getSaveFilePath());
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
            onDownloading(mDownloadRequest);
            executeDownload();
            //Download finish.
            finishDownload();
        } catch (DownloadErrorException e){
            DownloadLog.e(e.getMessage());
            onFailDownload(mDownloadRequest);
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
        mDownloadRequest.setStatus(Status.START);
        onStartDownload(mDownloadRequest);
        executorService.execute(this);
    }

    public void waitDownload(){
        mDownloadRequest.setStatus(Status.WAIT);
        onWaitDownload(mDownloadRequest);
        notifyWait();
    }

    /**
     * 开始下载调用
     */
    protected void onStartDownload(DownloadRequest downloadRequest){}

    /**
     * 正在下载调用
     */
    protected void onDownloading(DownloadRequest downloadRequest){
        mDownloadRequest.setStatus(Status.DOWNLOADING);
    }

    /**
     * 暂停下载调用
     */
    protected void onPauseDownload(DownloadRequest downloadRequest){}

    /**
     * 取消下载调用
     */
    protected void onCancelDownload(DownloadRequest downloadRequest){}

    /**
     * 完成下载
     */
    protected void onFinishDownload(DownloadRequest downloadRequest){}

    /**
     * 任务等待时候会调用
     * @param downloadRequest
     */
    protected void onWaitDownload(DownloadRequest downloadRequest){}

    /**
     * 下载失败时候会调用
     * @param downloadRequest 下载请求
     */
    protected void onFailDownload(DownloadRequest downloadRequest){
        mDownloadRequest.setStatus(Status.FAIL);
    }

    /**
     * 执行完成,不论下载失败成功都会调用
     * @param downloadRequest 下载请求
     */
    protected void onNextTask(DownloadRequest downloadRequest){}
    private void nextTask(){
        DownloadLog.e(String.format("Download status:%s",getStatus()));
        onNextTask(mDownloadRequest);
        if (mDownloadQueueManager != null){
            mDownloadQueueManager.nextTask(this);
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
            if (mSaveFile.delete()){
                DownloadLog.e("Delete old download file success:"+mSaveFile);
            }
        } else if (!mSaveFile.exists()){
            //文件不存在,重置下载大小为0.
            mDownloadRequest.setCurrentSize(0);
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
        final int oldStatus = mDownloadRequest.getStatus();
        mDownloadRequest.setStatus(Status.PAUSE);
        onPauseDownload(mDownloadRequest);
        if (oldStatus == Status.WAIT){
            notifyPause();
        }
    }

    public void cancelDownload() {
        final int oldStatus = mDownloadRequest.getStatus();
        mDownloadRequest.setStatus(Status.CANCEL);
        onCancelDownload(mDownloadRequest);
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
        return mDownloadRequest.getFailType();
    }

    public String getErrorMessage(){
        return mDownloadRequest.getFailMessage();
    }

    public int getStatus() {
        return mDownloadRequest.getStatus();
    }

    public boolean isSuccess() {
        return getStatus() == Status.FINISHED;
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
        return mDownloadRequest.getDownloadUrl();
    }

    public long getCurrentSize() {
        return mDownloadRequest.getCurrentSize();
    }

    public long getTotalSize() {
        return mDownloadRequest.getTotalSize();
    }

    public int getProgress() {
        return mDownloadRequest.getProgress();
    }

    public String getName() {
        return mSaveFile.getName();
    }

    public DownloadRequest getDownloadRequest(){
        return mDownloadRequest;
    }

    public File getFile(){
        return mSaveFile;
    }

    public void setDownloadQueueManager(DownloadQueueManager downloadQueueManager){
        this.mDownloadQueueManager = downloadQueueManager;
    }

    /**
     * Set downloadListener
     * @param downloadListener
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
                    mDownloadListener.onPause(mDownloadRequest,getDownloadUrl(),getProgress());
                }
            }
        });
    }

    void notifyRunning(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onRunning(mDownloadRequest);
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
                    mDownloadListener.onCancel(mDownloadRequest,getDownloadUrl());
                }
            }
        });
    }

    void notifyProgress(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onUpdateProgress(mDownloadRequest,getDownloadUrl(),getProgress());
                }
            }
        });
    }

    protected void notifyWait(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onWait(mDownloadRequest,getDownloadUrl());
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
                    mDownloadListener.onStart(mDownloadRequest);
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
                    mDownloadListener.onFail(mDownloadRequest,getDownloadUrl(),failType,getErrorMessage());
                }
            }
        });
    }

    void notifyFinish(){
        postNotify(new Runnable() {
            @Override
            public void run() {
                if(mDownloadListener != null){
                    mDownloadListener.onFinish(mDownloadRequest,getDownloadUrl(),mSaveFile);
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
        mDownloadRequest.setStatus(Status.FAIL);
        mDownloadRequest.setFailType(failType);
        mDownloadRequest.setFailMessage(failMessage);
    }

    protected void handlerNetworkFail(){
        mDownloadRequest.setStatus(Status.FAIL);
        mDownloadRequest.setFailType(ErrorType.NETWORK_ERROR);
        mDownloadRequest.setFailMessage(mContext.getString(R.string.download_network_error));
    }

    /**
     * 更新DownloadRequest
     * @param readLen 读取长度
     */
    private void updateDownloadRequest(long readLen){
        mDownloadRequest.setCurrentSize(readLen+getCurrentSize());
        final int downloadProgress = Utils.getProgress(getCurrentSize(),getTotalSize());
        mDownloadRequest.setProgress(downloadProgress);
        onUpdateProgress(mDownloadRequest.getTotalSize(),mDownloadRequest.getCurrentSize(),downloadProgress);
        notifyProgress();
    }

    protected void finishDownload(){
        if(mDownloadQueueManager != null){
            mDownloadQueueManager.finishTask(this);
        }
        mDownloadRequest.setStatus(Status.FINISHED);
        onFinishDownload(mDownloadRequest);
        notifyFinish();
    }

    protected void throwDownloadErrorException(String message)throws DownloadErrorException{
        throw new DownloadErrorException(message);
    }

    protected void createParentFile()throws DownloadErrorException{
        synchronized (LOCK){
            File parentFile = mSaveFile.getParentFile();
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
            if(!isCancelDownload() && !mSaveFile.exists()){
                pauseDownload();
                return;
            }
            randomAccessFile.write(buffers,0,readLen);
            updateDownloadRequest(readLen);
        } catch (IOException e){
            handlerDownloadFail(ErrorType.IO_ERROR,mContext.getString(R.string.download_io_error));
            throwDownloadErrorException(String.format("Write file name:%s,error:%s",mSaveFile.getAbsolutePath(),e.getMessage()));
        }
    }

    protected RandomAccessFile createAndSeekFile(long position)throws DownloadErrorException{
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(mSaveFile,"rw");
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
            throw new DownloadErrorException(String.format("Read buffer fail:%s,errorMessage:%s",mSaveFile.getAbsolutePath(),e.getMessage()));
        }
    }
}