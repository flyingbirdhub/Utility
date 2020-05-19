package org.cloud;

public class ConvertException extends Exception {
    public ConvertException(String msg){
        super(msg);
    }

    public ConvertException(Throwable cause){
        super(cause);
    }
}
