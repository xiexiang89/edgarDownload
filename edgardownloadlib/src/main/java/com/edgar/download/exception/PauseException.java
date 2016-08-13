package com.edgar.download.exception;

/**
 * Created by edgar on 2016/4/2.
 */
public class PauseException extends Exception{

    public PauseException(){
        super();
    }

    public PauseException(String message){
        super(message);
    }
}