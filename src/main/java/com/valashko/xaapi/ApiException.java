package com.valashko.xaapi;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
