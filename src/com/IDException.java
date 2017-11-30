package com;

public class IDException extends Exception{
    public IDException() {}
    public IDException(String message) {
        super(message);
    }
    public IDException(String message, Throwable cause) {
        super(message, cause);
    }
    public IDException(Throwable cause) {
        super(cause);
    }
}
