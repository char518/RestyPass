package com.github.df.restypass.exception.execute;

/**
 * 请求连接异常（httpConnectException, TimeoutException,InterruptException）
 * Created by darrenfu on 17-7-22.
 */
@SuppressWarnings("unused")
public class FallbackException extends RestyException {

    public FallbackException(Throwable throwable) {
        super(throwable);
    }

    public FallbackException(String msg) {
        super(msg);
    }

    public FallbackException(String code, String msg) {
        super(code, msg);
    }
}
