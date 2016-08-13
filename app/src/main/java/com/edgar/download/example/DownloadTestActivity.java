package com.edgar.download.example;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edgar.download.DownloadQueueManager;
import com.edgar.download.DownloadRequest;
import com.edgar.download.observe.DownloadListener;
import com.edgar.download.task.AbsDownloadTask;
import com.edgar.download.task.HttpDownloadTask;

import java.io.File;

/**
 * Created by Edgar on 2016/7/6.
 */
public class DownloadTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String KEY_TOTAL_SIZE = "totalSize";
    private static final String KEY_CURRENT_SIZE = "currentSize";
    private static final String DOWNLOAD_URL = "http://shouji.360tpcdn.com/160701/1fb9841fd163ad0ca6ad277e5121aa68/com.happyelements.AndroidAnimal_34.apk";
    private static final File SAVE_PATH = new File(Environment.getExternalStorageDirectory(),"edgar/pao_1.0.34.0_134.apk");
    private ProgressBar mDownloadProgressBar;
    private TextView mDownloadProgressText;
    private Button mStartDownload;
    private DownloadQueueManager mDownloadQueueManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor mEditor;
    private AbsDownloadTask downloadTask;
    private long mLastCurrentTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);
        mDownloadProgressBar = (ProgressBar) findViewById(R.id.download_progress);
        mDownloadProgressText = (TextView) findViewById(R.id.download_progress_text);
        mStartDownload = (Button) findViewById(R.id.start_download);
        mStartDownload.setOnClickListener(this);
        mDownloadQueueManager = DownloadQueueManager.getInstance(this);
//        sharedPreferences = getSharedPreferences("downloadConfig", Context.MODE_APPEND);
//        mEditor = sharedPreferences.edit();
    }

    @Override
    public void onClick(View v) {
        if (downloadTask == null){
            mLastCurrentTime = System.currentTimeMillis();
            downloadTask = new HttpDownloadTask(this,buildDownloadRequest()){
                @Override
                protected void onRequestOk(DownloadRequest downloadRequest) {
                    super.onRequestOk(downloadRequest);
//                    mEditor.putLong(KEY_TOTAL_SIZE,downloadRequest.getTotalSize()).apply();
                }
            };
            downloadTask.setDownloadListener(new DownloadListener() {
                @Override
                public void onRunning(DownloadRequest downloadRequest) {
                }

                @Override
                public void onStart(DownloadRequest downloadRequest) {

                }

                @Override
                public void onWait(DownloadRequest downloadRequest, String url) {

                }

                @Override
                public void onPause(DownloadRequest downloadRequest, String url, int progress) {

                }

                @Override
                public void onFinish(DownloadRequest downloadRequest, String url, File filePath) {

                }

                @Override
                public void onFail(DownloadRequest downloadRequest, String url, int errorCode, String errorMsg) {

                }

                @Override
                public void onCancel(DownloadRequest downloadRequest, String url) {

                }

                @Override
                public void onUpdateProgress(DownloadRequest downloadRequest, String url, int progress) {
//                    mEditor.putLong(KEY_CURRENT_SIZE,downloadRequest.getCurrentSize()).apply();
                    mDownloadProgressBar.setProgress(progress);
                    String download =
                            String.format("%s/s  已下载:%s",
                                    Formatter.formatFileSize(DownloadTestActivity.this,makeSpeed(downloadRequest.getCurrentSize())),
                                    Formatter.formatFileSize(DownloadTestActivity.this,downloadRequest.getCurrentSize()));
                    mStartDownload.setText(download);
                    mDownloadProgressText.setText(String.format("下载进度:%s",progress));
                }
            });
            mDownloadQueueManager.addDownloadTask(downloadTask);
        } else {
            if (downloadTask.isDownloading()){
                mDownloadQueueManager.pauseDownload(downloadTask);
            } else {
                mDownloadQueueManager.addDownloadTask(downloadTask);
            }
        }
    }

    private long makeSpeed(long currentSize){
        long curTime = System.currentTimeMillis();
        int usedTime = (int) ((curTime-mLastCurrentTime)/1000);

        if(usedTime==0)usedTime = 1;
        return (currentSize/usedTime);
    }

    private DownloadRequest buildDownloadRequest(){
        return new DownloadRequest().setDownloadUrl(DOWNLOAD_URL)
                .setSaveFilePath(SAVE_PATH.getAbsolutePath());
    }
}