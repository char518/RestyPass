package com.github.df.restypass.command;

import com.github.df.restypass.exception.execute.ConnectionException;
import com.github.df.restypass.http.converter.ResponseConverterContext;
import com.github.df.restypass.http.pojo.FailedResponse;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.LongAdder;

/**
 * Resty Future
 * 异步获取RestyCommand的响应结果
 * Created by darrenfu on 17-7-20.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Slf4j
public class RestyFuture implements Future<Response> {

    public static LongAdder time = new LongAdder();
    public static LongAdder count = new LongAdder();

    private RestyCommand restyCommand;

    private ListenableFuture<Response> future;

    private ResponseConverterContext converterContext;

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
    public Response get() {
        long start = System.currentTimeMillis();

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            future.abort(e);
            log.error("获取响应失败:{}", e.getMessage());
            return FailedResponse.create(new ConnectionException(e));
        } finally {
            long end = System.currentTimeMillis();
            count.increment();
            time.add(end - start);
        }
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public Response get(long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.abort(e);
            log.error("获取响应失败:{}", e.getMessage());
            return FailedResponse.create(new ConnectionException(e));
        }
    }


    /**
     * Async resty future.
     *
     * @param obj the obj
     * @return the resty future
     */
    public static RestyFuture async(Object obj) {
        if (obj instanceof RestyFuture) {
            return (RestyFuture) obj;
        }
        return null;
    }
}
