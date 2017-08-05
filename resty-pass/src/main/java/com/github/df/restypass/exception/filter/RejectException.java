package com.github.df.restypass.exception.filter;

/**
 * Filter 拒绝请求异常类
 * <p>
 * Created by darrenfu on 17-7-22.
 */
@SuppressWarnings("unused")
public class RejectException extends RuntimeException {

    private static String RESTY_EXCEPTION_CODE = "RESTY_EXCEPTION";

    private String code;

    public RejectException(String msg) {
        super(msg);
        this.code = RESTY_EXCEPTION_CODE;
    }


    public RejectException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public RejectException(Throwable throwable) {
        super(throwable);
        this.code = RESTY_EXCEPTION_CODE;
    }
}
