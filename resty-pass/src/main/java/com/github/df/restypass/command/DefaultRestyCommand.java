package com.github.df.restypass.command;

import com.github.df.restypass.cb.CircuitBreaker;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.lb.server.ServerInstance;
import lombok.Data;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * 默认Resty请求命令
 * 封装请求内容，实现请求过程
 * Created by darrenfu on 17-6-20.
 */
@Data
public class DefaultRestyCommand implements RestyCommand {

    private static final Logger log = LoggerFactory.getLogger(DefaultRestyCommand.class);

    /**
     * request 模板
     */
    private RestyRequestTemplate requestTemplate;

    /**
     * command请求路径 [requestTemplate]
     * 如果有PathVariable，此处的path获取的是原始路径(如:/find/{name})，而不是参数替换后的路径
     */
    private String path;

    /**
     * Get or Post [requestTemplate]
     */
    private String httpMethod;

    /**
     * Class
     */
    private Class serviceClz;

    /**
     * Method
     */
    private Method serviceMethod;

    /**
     * 返回类型
     */
    private Type returnType;

    /**
     * 方法参数列表
     */
    private Object[] args;

    /**
     * 上下文context
     */
    private RestyCommandContext context;

    /**
     * command 配置
     */
    private RestyCommandConfig restyCommandConfig;
    /**
     * 服务名称 [restyCommandConfig]
     */
    private String serviceName;

    /**
     * 状态
     */
    private RestyCommandStatus status;

    /**
     * 导致Command失败的异常
     */
    private RestyException exception;

    /**
     * 向服务请求的request
     */
    private Request request;

    /**
     * Uri
     */
    private Uri uri;

    /**
     * 使用的断路器
     */
    private CircuitBreaker circuitBreaker;

    /**
     * command执行访问的服务实例（最后一次）
     */
    private ServerInstance instance;

    /**
     * 是否是异步命令-基于返回值future
     * 示例接口:
     * Future<User> getUser(String userId);
     **/
    private boolean asyncFutureReturn;


    /**
     * 是否是异步命令-基于请求参数future
     * 示例接口:User getUser(String userId, RestyFuture<User>);
     */
    private boolean asyncFutureArg;


    public DefaultRestyCommand(Class serviceClz,
                               Method serviceMethod,
                               Type returnTyp,
                               Object[] args,
                               RestyCommandContext context) {
        this.serviceClz = serviceClz;
        this.serviceMethod = serviceMethod;
        this.returnType = returnTyp;
        this.args = args;
        this.context = context;
        this.status = RestyCommandStatus.INIT;

        if (context.getCommandProperties(serviceMethod) == null) {
            throw new IllegalArgumentException("缺少CommandProperties，无法初始化RestyCommand");
        }

        this.restyCommandConfig = context.getCommandProperties(serviceMethod);
        this.serviceName = restyCommandConfig.getServiceName();

        this.requestTemplate = context.getRequestTemplate(serviceMethod);
        this.httpMethod = requestTemplate.getHttpMethod();
        this.path = requestTemplate.getPath();

        isAsyncCommand();
    }


    @Override
    public Uri getUri(ServerInstance serverInstance) {
        if (uri == null) {
            uri = new Uri(serverInstance.getIsHttps() ? HTTPS : HTTP,
                    null,
                    serverInstance.getHost(),
                    serverInstance.getPort(),
                    requestTemplate.getRequestPath(args),
                    requestTemplate.getQueryString(args));
        }
        return uri;
    }

    @Override
    public boolean isAsyncReturn() {
        return this.asyncFutureReturn;
    }

    @Override
    public boolean isAsyncArg() {
        return this.asyncFutureArg;
    }

    /**
     * 是否是异步请求
     *
     * @return 是异步：true, 不是异步请求：false
     */
    private boolean isAsyncCommand() {
        Type returnType = this.getReturnType();
        this.asyncFutureReturn = false;
        this.asyncFutureArg = false;

        if (returnType instanceof ParameterizedTypeImpl) {
            //返回类型是Future
            ParameterizedTypeImpl returnParameterType = (ParameterizedTypeImpl) returnType;
            if (returnParameterType.getRawType() == Future.class
                    && returnParameterType.getActualTypeArguments() != null
                    && returnParameterType.getActualTypeArguments().length > 0) {
                //替换return type: Future<User> -> User
                this.returnType = returnParameterType.getActualTypeArguments()[0];
                this.asyncFutureReturn = true;
                return true;
            }
        }
        RestyFuture futureArg = getFutureArg(this);
        if (futureArg != null) {
            /**
             * Future类型出参
             * command执行的时候处理此类异步，将执行结果future放入futureArg中
             * @see DefaultRestyCommand#start
             */
            this.asyncFutureArg = true;
            return true;
        }
        return false;
    }

    private RestyFuture getFutureArg(RestyCommand restyCommand) {
        if (restyCommand.getArgs() != null && restyCommand.getArgs().length > 0) {
            for (Object arg : restyCommand.getArgs()) {
                if (arg != null && arg instanceof RestyFuture) {
                    return (RestyFuture) arg;
                }
            }
        }
        return null;
    }


    @Override
    public RestyCommandStatus getStatus() {
        return this.status;
    }

    @Override
    public RestyCommand ready(CircuitBreaker cb) {
        this.circuitBreaker = cb;
        this.status = RestyCommandStatus.READY;
        return this;
    }

    @Override
    public RestyFuture start(ServerInstance instance) {
        this.status = RestyCommandStatus.STARTED;
        this.instance = instance;

        AsyncHttpClient httpClient = context.getHttpClient(this.getServiceName());
        BoundRequestBuilder requestBuilder = new BoundRequestBuilder(httpClient,
                httpMethod,
                true);
        requestBuilder.setUri(this.getUri(instance));
        requestBuilder.setSingleHeaders(requestTemplate.getRequestHeaders(args));
        requestBuilder.setBody(requestTemplate.getBody(args));
        this.request = requestBuilder.build();

        ListenableFuture<Response> future;
        RestyFuture restyFuture = new RestyFuture();
        restyFuture.setRestyCommand(this);
        try {
            future = requestBuilder.execute(SingletonAsyncErrorHandler.handler);
//            future = httpClient.executeRequest(request);
        } catch (Exception e) {
            future = new ErrorFuture<>(e);
        }
        restyFuture.setFuture(future);

        if (log.isDebugEnabled()) {
            log.debug("Request:{}", request);
        }
        if (this.asyncFutureArg) {
            RestyFuture futureArg = getFutureArg(this);
            futureArg.setFuture(future);
            futureArg.setRestyCommand(this);
        }
        return restyFuture;
    }


    @Override
    public String getInstanceId() {
        if (this.instance == null) {
            throw new RuntimeException("instance is null");
        }
        return this.instance.getInstanceId();
    }

    @Override
    public void success() {
        this.status = RestyCommandStatus.SUCCESS;
        if (log.isDebugEnabled()) {
            log.debug("请求成功:{} @ {}", this.getPath(), this.instance);
        }
        this.emit(circuitBreaker.getEventKey(), this);
    }

    @Override
    public void failed(RestyException restyException) {
        this.exception = restyException;
        this.status = RestyCommandStatus.FAILED;
        if (log.isDebugEnabled()) {
            log.debug("请求失败:{}@{}: {}", this.getPath(), this.instance, Arrays.toString(this.args));
        }
        this.emit(circuitBreaker.getEventKey(), this);
        throw exception;
    }


    @Override
    public RestyException getFailException() {
        return this.exception;
    }


    static class SingletonAsyncErrorHandler extends AsyncCompletionHandlerBase {

        static SingletonAsyncErrorHandler handler = new SingletonAsyncErrorHandler();

        SingletonAsyncErrorHandler() {

        }

        @Override
        public void onThrowable(Throwable t) {

            // do nothing
        }

    }


    class ErrorFuture<T> implements ListenableFuture<T> {

        private final ExecutionException e;

        public ErrorFuture(Throwable t) {
            e = new ExecutionException(t);
        }

        public ErrorFuture(String message, Throwable t) {
            e = new ExecutionException(message, t);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
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
        public T get() throws InterruptedException, ExecutionException {
            throw e;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw e;
        }

        @Override
        public void done() {
        }

        @Override
        public void abort(Throwable t) {
        }

        @Override
        public void touch() {
        }

        @Override
        public ListenableFuture<T> addListener(Runnable listener, Executor exec) {
            if (exec != null) {
                exec.execute(listener);
            } else {
                listener.run();
            }
            return this;
        }

        @Override
        public CompletableFuture<T> toCompletableFuture() {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

}
