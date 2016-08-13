package com.edgar.download.exception;

/**
 * Created by edgar on 2016/4/2.
 */
public class StopException extends Exception{

    public StopException(){
        super();
    }

    public StopException(String message){
        super(message);
    }
}