package com.edgar.download.task;

import android.content.Context;

import com.edgar.download.ErrorType;
import com.edgar.download.utils.Utils;
import com.edgar.download.DownloadConstant;
import com.edgar.download.DownloadLog;
import com.edgar.download.DownloadRequest;
import com.edgar.download.R;
import com.edgar.download.exception.DownloadErrorException;
import com.edgar.download.exception.PauseException;
import com.edgar.download.exception.StopException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Edgar on 2016/4/18.
 * Http 下载任务
 */
public class HttpDownloadTask extends AbsDownloadTask{

    private static final int HTTP_TEMP_REDIRECT = 307;  //重定向
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String RANGE = "Range";
    private static final String LOCATION = "Location";
    private static final String CONTENT_LENGTH = "Content-Length";
    private final OkHttpClient.Builder mOkHttpBuilder = new OkHttpClient.Builder();

    public HttpDownloadTask(Context context,DownloadRequest downloadRequest) {
        super(context,downloadRequest);
    }

    @Override
    protected void executeDownload() throws DownloadErrorException,StopException,PauseException {
        int retryCount = 1;
        String downloadUrl = null;
        while (hasAttemptRemaining(retryCount++)){
            try {
                //2.检查用户是否有暂停、停止动作
                checkUserPauseOrStop();
                //3.创建请求对象
                downloadUrl = getDownloadUrl();
                Request.Builder builder = new Request.Builder();
                builder.get().url(downloadUrl);
                //4.添加header
                addRequestHeader(builder);
                //5.设置超时
                setTimeOut();
                //6.执行请求
                Response response = mOkHttpBuilder.build().newCall(builder.build()).execute();
                final int responseCode = response.code();
                final String responseMessage = response.message();
                DownloadLog.e(String.format("url:%s   [statusCode:%s,responseMessage:%s]",
                        downloadUrl,responseCode,responseMessage));
                switch (responseCode){
                    case HttpURLConnection.HTTP_OK:
                        //处理请求状态为200
                        parserOKHeader(response);
                        checkStorageNotEnough(mDownloadRequest.getTotalSize());
                        transferData(response);
                        return;
                    case HttpURLConnection.HTTP_PARTIAL:
                        //处理请求状态为206，断点续传会返回该状态码.
                        onRequestPartial(mDownloadRequest);
                        checkStorageNotEnough(mDownloadRequest.getTotalSize());
                        transferData(response);
                        return;
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                    case HttpURLConnection.HTTP_SEE_OTHER:
                    case HTTP_TEMP_REDIRECT:
                        //资源被移动到别的地方.
                        String location = response.header(LOCATION);
                        if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
                                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                            //重定向301,指向新的url下载
                            mDownloadRequest.setDownloadUrl(location);
                            onResourceMoved(mDownloadRequest);
                            continue;
                        } else {
                            handlerDownloadFail(responseCode,responseMessage);
                            throw new DownloadErrorException(
                                    String.format("Url location move:%s responseMessage=%s",location,
                                            responseMessage));
                        }
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        //请求的资源被存在,返回的是404.
                        handlerDownloadFail(ErrorType.NOT_CONTENT,responseMessage);
                        throw new DownloadErrorException("Not find content:"+downloadUrl);
                    default:
                        if(responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                                || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR
                                || responseCode == HttpURLConnection.HTTP_UNAVAILABLE){
                            //If statusCode is server error,the throw DownloadError.
                            handlerDownloadFail(ErrorType.SERVER_ERROR,
                                    getContext().getString(R.string.download_server_error,responseMessage));
                            throw new DownloadErrorException(String.format("Server error:%s,%s",responseCode,responseMessage));
                        }
                }
            } catch (IOException e){
                //检查用户是否有暂停、停止下载的动作
                checkUserPauseOrStop();
                handlerNetworkFail();
                DownloadErrorException downloadError =
                        new DownloadErrorException(String.format("Download url:%s,  " +
                                "request download error:%s",downloadUrl,e.getMessage()));
                retry(retryCount,downloadError);
            }
        }
    }

    private void addRequestHeader(Request.Builder builder){
        builder.addHeader(ACCEPT_ENCODING, "identity");
        long currentSize = getCurrentSize();
        if (currentSize > 0){
            builder.addHeader(RANGE,"bytes="+currentSize+"-");
        }
    }

    private void setTimeOut(){
        DownloadRequest downloadRequest = getDownloadRequest();
        mOkHttpBuilder.connectTimeout(downloadRequest.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(downloadRequest.getTimeoutMs(),TimeUnit.MILLISECONDS);
    }

    private void retry(int currentRetryCount,DownloadErrorException error) throws DownloadErrorException {
        int currentTimeoutMs = mDownloadRequest.getTimeoutMs();
        currentTimeoutMs += (currentTimeoutMs * DownloadConstant.DEFAULT_BACKOFF_MULT);
        mDownloadRequest.setTimeoutMs(currentTimeoutMs);
        if (!hasAttemptRemaining(currentRetryCount)) {
            throw error;
        }
    }

    private boolean hasAttemptRemaining(int currentRetryCount) {
        return currentRetryCount <= mDownloadRequest.getMaxRetryCount();
    }

    protected void onRequestOk(DownloadRequest downloadRequest){}
    protected void onRequestPartial(DownloadRequest downloadRequest){}
    protected void onResourceMoved(DownloadRequest downloadRequest){}

    /**
     * 解析请求成功的header字段
     * @param response 连接对象
     */
    private void parserOKHeader(Response response){
        mDownloadRequest.setTotalSize(Utils.strToLong(response.header(CONTENT_LENGTH),0));
        onRequestOk(mDownloadRequest);
    }

    /**
     * get header field convert long
     * @param conn 连接对象
     * @param field 字段名
     * @param defaultValue 默认值
     * @return
     */
    private long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * connection url.
     * @param url Request download url.
     * @return New HttpUrlConnection
     * @throws IOException If openConnection error,the throw IOException.
     */
    private HttpURLConnection openConnection(URL url)throws IOException{
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(mDownloadRequest.getTimeoutMs());
            httpURLConnection.setConnectTimeout(mDownloadRequest.getTimeoutMs());
            return httpURLConnection;
        } catch (IOException e) {
            DownloadLog.e(String.format("Download url:%s  " +
                    "URL openConnection fail:%s",url.toString(),e.getMessage()));
            throw e;
        }
    }

    private void transferData(Response response)throws DownloadErrorException,
            PauseException,StopException{
        checkUserPauseOrStop();
        notifyStart();
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            inputStream = new BufferedInputStream(response.body().byteStream());
            randomAccessFile = createAndSeekFile(getCurrentSize());
            byte[] buffers = new byte[DownloadConstant.DEFAULT_BUFFER_SIZE];
            while (true){
                checkUserPauseOrStop();
                int readLen = readBufferFromNetwork(inputStream,buffers);
                if(readLen != -1){
                    writeToFile(randomAccessFile,buffers,readLen);
                } else {
                    break;
                }
            }
        } finally {
            Utils.closeQuietly(inputStream);
            Utils.closeQuietly(randomAccessFile);
        }
    }
}