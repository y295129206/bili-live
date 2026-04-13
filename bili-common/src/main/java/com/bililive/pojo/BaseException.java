package com.bililive.pojo;

import lombok.Data;

@Data
public class BaseException extends RuntimeException {

    private Integer code;
    private String msg;

    public BaseException(String message) {
        super(message);
        this.code = 0;
        this.msg = message;
    }
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }
}