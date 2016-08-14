package com.edgar.download.example;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edgar.download.DownloadManager;
import com.edgar.download.DownloadRequest;
import com.edgar.download.observe.DefaultDownloadListener;
import com.edgar.download.task.AbsDownloadTask;
import com.edgar.download.task.HttpDownloadTask;

import java.io.File;

/**
 * Created by Edgar on 2016/7/6.
 */
public class DownloadTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String DOWNLOAD_URL = "http://shouji.360tpcdn.com/160701/1fb9841fd163ad0ca6ad277e5121aa68/com.happyelements.AndroidAnimal_34.apk";
    private static final File SAVE_PATH = new File(Environment.getExternalStorageDirectory(),"edgar/pao_1.0.34.0_134.apk");
    private ProgressBar mDownloadProgressBar;
    private TextView mDownloadProgressText;
    private Button mStartDownload;
    private DownloadManager mDownloadManager;
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
        mDownloadManager = DownloadManager.getInstance(this);
    }

    @Override
    public void onClick(View v) {
        if (downloadTask == null){
            mLastCurrentTime = System.currentTimeMillis();
            downloadTask = mDownloadManager.addDownloadTask(buildDownloadRequest());
            downloadTask.setDownloadListener(new DefaultDownloadListener() {
                @Override
                public void onUpdateProgress(AbsDownloadTask downloadTask, long totalSize, long currentSize, int progress) {
                    mDownloadProgressBar.setProgress(progress);
                    String download =
                            String.format("%s/s  已下载:%s",
                                    Formatter.formatFileSize(DownloadTestActivity.this,makeSpeed(currentSize)),
                                    Formatter.formatFileSize(DownloadTestActivity.this,currentSize));
                    mStartDownload.setText(download);
                    mDownloadProgressText.setText(String.format("下载进度:%s",progress));
                }
            });
        } else {
            if (downloadTask.isDownloading()){
                mDownloadManager.pauseDownload(downloadTask);
            } else {
                mDownloadManager.resumeDownloadTask(downloadTask);
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
        return new DownloadRequest.Builder().downloadUrl(DOWNLOAD_URL).configSaveFilePath(SAVE_PATH.getAbsolutePath()).build();
    }
}