package com.github.df.restypass.command;

import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.ConnectionException;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.http.converter.ResponseConverterContext;
import com.github.df.restypass.http.pojo.FailedResponse;
import lombok.Getter;
import lombok.Setter;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Resty Future
 * 异步获取RestyCommand的响应结果
 * Created by darrenfu on 17-7-20.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestyFuture<T> implements Future<T> {
    private static final Logger log = LoggerFactory.getLogger(RestyFuture.class);

    @Getter
    @Setter
    private RestyCommand restyCommand;

    @Getter
    @Setter
    private ListenableFuture<Response> future;

    private ResponseConverterContext converterContext;

    public RestyFuture() {
        //can not work directly,
        // use this when you want to get response async
        // command executor will fill content in this object
    }

    /**
     * Instantiates a new Resty future.
     *
     * @param restyCommand the resty command
     * @param future       the future
     */
    public RestyFuture(RestyCommand restyCommand, ListenableFuture<Response> future) {
        this.restyCommand = restyCommand;
        this.future = future;
        this.converterContext = ResponseConverterContext.DEFAULT;
    }

    /**
     * Instantiates a new Resty future.
     *
     * @param restyCommand     the resty command
     * @param future           the future
     * @param converterContext the converter context
     */
    public RestyFuture(RestyCommand restyCommand, ListenableFuture<Response> future, ResponseConverterContext converterContext) {
        this.restyCommand = restyCommand;
        this.future = future;
        this.converterContext = converterContext;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws RestyException {
        Response response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            future.abort(e);
            log.warn("获取响应失败:{}", e.getMessage());
            if (this.getRestyCommand().isAsyncArg() || this.getRestyCommand().isAsyncReturn()) {
                throw new RestyException(e);
            }
            response = FailedResponse.create(new ConnectionException(e));
        }
        T resp = (T) ResponseConverterContext.DEFAULT.convertResponse(restyCommand, response);
        if (restyCommand.isAsyncReturn() || restyCommand.isAsyncArg()) {
            if (RestyCommandStatus.FAILED == restyCommand.getStatus() && restyCommand.getFailException() != null) {
                throw restyCommand.getFailException();
            }
        }

        return resp;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public T get(long timeout, TimeUnit unit) throws RestyException {
        Response response;

        try {
            response = future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.abort(e);
            log.error("获取响应失败:{}", e.getMessage());
            if (this.getRestyCommand().isAsyncArg() || this.getRestyCommand().isAsyncReturn()) {
                throw new RestyException(e);
            }
            response = FailedResponse.create(new ConnectionException(e));
        }
        T resp = (T) ResponseConverterContext.DEFAULT.convertResponse(restyCommand, response);
        if (restyCommand.isAsyncReturn() || restyCommand.isAsyncArg()) {
            if (RestyCommandStatus.FAILED == restyCommand.getStatus() && restyCommand.getFailException() != null) {
                throw restyCommand.getFailException();
            }
        }

        return resp;
    }


    public static class ErrorRestyFuture extends RestyFuture {
        private Exception exception;

        public ErrorRestyFuture(Exception exception) {
            this.exception = exception;
        }


        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Exception get() {
            return this.exception;
        }


        @SuppressWarnings("NullableProblems")
        @Override
        public Exception get(long timeout, TimeUnit unit) {
            return this.exception;
        }

    }
}
