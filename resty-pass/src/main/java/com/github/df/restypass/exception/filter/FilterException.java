package com.github.df.restypass.exception.filter;

/**
 * Filter 基础异常类
 * <p>
 * Created by darrenfu on 17-7-22.
 */
@SuppressWarnings("unused")
public class FilterException extends RuntimeException {

    private static String RESTY_EXCEPTION_CODE = "RESTY_EXCEPTION";

    private String code;

    public FilterException(String msg) {
        super(msg);
        this.code = RESTY_EXCEPTION_CODE;
    }


    public FilterException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public FilterException(Throwable throwable) {
        super(throwable);
        this.code = RESTY_EXCEPTION_CODE;
    }
}
