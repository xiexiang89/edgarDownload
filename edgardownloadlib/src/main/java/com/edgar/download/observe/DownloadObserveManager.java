package com.edgar.download.observe;

import com.qida.download.DownloadRequest;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Edgar on 2016/4/21.
 */
public class DownloadObserveManager implements DownloadListener{
    //监听器
    private List<DownloadListener> mObserveList =
            Collections.synchronizedList(new LinkedList<DownloadListener>());
    @Override
    public void onRunning(DownloadRequest downloadRequest) {
        notifyRunning(downloadRequest);
    }

    @Override
    public void onStart(DownloadRequest downloadRequest) {
        notifyStartDownload(downloadRequest);
    }

    @Override
    public void onWait(DownloadRequest downloadRequest, String url) {
        notifyDownloadWait(downloadRequest,url);
    }

    @Override
    public void onPause(DownloadRequest downloadRequest, String url, int progress) {
        notifyPauseDownload(downloadRequest,url,progress);
    }

    @Override
    public void onFinish(DownloadRequest downloadRequest, String url, File filePath) {
        notifyDownloadFinish(downloadRequest,url,filePath);
    }

    @Override
    public void onFail(DownloadRequest downloadRequest, String url, int errorCode, String errorMsg) {
        notifyDownloadFail(downloadRequest,errorCode,errorMsg);
    }

    @Override
    public void onCancel(DownloadRequest downloadRequest, String url) {
        notifyCancelDownload(downloadRequest,url);
    }

    @Override
    public void onUpdateProgress(DownloadRequest downloadRequest, String url, int progress) {
        notifyUpdateProgress(downloadRequest,url,progress);
    }


    public void registerDownloadListener(DownloadListener downloadListener){
        if(downloadListener != null && !mObserveList.contains(downloadListener)){
            mObserveList.add(downloadListener);
        }
    }

    public void unRegisterDownloadListener(DownloadListener downloadListener){
        if(downloadListener != null){
            mObserveList.remove(downloadListener);
        }
    }

    private void notifyRunning(final DownloadRequest downloadRequest){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onRunning(downloadRequest);
                }
            }
        });
    }

    private void notifyStartDownload(final DownloadRequest downloadRequest){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onStart(downloadRequest);
                }
            }
        });
    }

    private void notifyPauseDownload(final DownloadRequest downloadRequest, final String url, final int progress){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onPause(downloadRequest,
                            url,progress);
                }
            }
        });
    }

    private void notifyCancelDownload(final DownloadRequest downloadRequest, final String url){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onCancel(downloadRequest, url);
                }
            }
        });
    }

    private void notifyUpdateProgress(final DownloadRequest downloadRequest, final String url, final int progress){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onUpdateProgress(downloadRequest,
                            url, progress);
                }
            }
        });
    }

    private void notifyDownloadFail(final DownloadRequest downloadRequest, final int errorCode, final String errorMsg){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if (downloadListener != null){
                    downloadListener.onFail(downloadRequest,
                            downloadRequest.getDownloadUrl(),
                            errorCode,errorMsg);
                }
            }
        });
    }

    private void notifyDownloadFinish(final DownloadRequest downloadRequest,final String url,final File file){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onFinish(downloadRequest, url,file);
                }
            }
        });
    }

    private void notifyDownloadWait(final DownloadRequest downloadRequest, final String url){
        notifyObserve(new IFindListener() {
            @Override
            public void onFind(DownloadListener downloadListener) {
                if(downloadListener != null){
                    downloadListener.onWait(downloadRequest,url);
                }
            }
        });
    }

    private void notifyObserve(IFindListener listener){
        if(mObserveList != null && mObserveList.size() > 0){
            for (DownloadListener downloadListener : mObserveList){
                if(listener != null){
                    listener.onFind(downloadListener);
                }
            }
        }
    }

    private interface IFindListener{
        void onFind(DownloadListener downloadListener);
    }
}
