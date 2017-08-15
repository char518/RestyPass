package com.github.df.restypass.command;

import com.github.df.restypass.cb.CircuitBreaker;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.lb.server.ServerInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 默认Resty请求命令
 * 封装请求内容，实现请求过程
 * Created by darrenfu on 17-6-20.
 */
@Data
@Slf4j
public class DefaultRestyCommand implements RestyCommand {

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
        requestBuilder.setSingleHeaders(requestTemplate.getHeaders());
        requestBuilder.setBody(requestTemplate.getBody(args));
        this.request = requestBuilder.build();
        ListenableFuture<Response> future = httpClient.executeRequest(request);

        if (log.isDebugEnabled()) {
            log.debug("Request:{}", request);
        }

        return new RestyFuture(this, future);
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


}
