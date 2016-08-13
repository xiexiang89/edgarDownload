package com.edgar.download.exception;

/**
 * Created by edgar on 2016/4/2.
 */
public class DownloadErrorException extends Exception{

    public DownloadErrorException(){
        super();
    }

    public DownloadErrorException(String message){
        super(message);
    }

    public DownloadErrorException(Throwable e){
        super(e);
    }

    public DownloadErrorException(String message,Throwable e){
        super(message,e);
    }
}