package com.github.df.restypass.executor;

import com.github.df.restypass.cb.CircuitBreaker;
import com.github.df.restypass.cb.CircuitBreakerFactory;
import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.command.RestyFuture;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.CircuitBreakException;
import com.github.df.restypass.http.converter.ResponseConverterContext;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.ServerContext;
import com.github.df.restypass.lb.server.ServerInstance;
import lombok.Setter;
import org.asynchttpclient.Response;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 异步Resty请求执行器
 * Created by darrenfu on 17-7-1.
 */
@SuppressWarnings("unused")
public class RestyCommandExecutor implements CommandExecutor {

    /**
     * Resty请求上下文
     */
    private RestyCommandContext context;

    /**
     * 服务实例上下文
     */
    @Setter
    private ServerContext serverContext;

    public RestyCommandExecutor(RestyCommandContext context) {
        this.context = context;
    }

    public RestyCommandExecutor(RestyCommandContext context, ServerContext serverContext) {
        this.context = context;
        this.serverContext = serverContext;
    }

    @Override
    public boolean executable(RestyCommand restyCommand) {
        return restyCommand != null && restyCommand.getStatus() == RestyCommandStatus.INIT;
    }

    @Override
    public Object execute(LoadBalancer lb, RestyCommand restyCommand) {

        if (restyCommand.getRestyCommandConfig().isForceBreakEnabled()) {
            throw new CircuitBreakException("circuit breaker is forced to open");
        }

        // 重试次数
        int retry = restyCommand.getRestyCommandConfig().getRetry();

        Object result = null;
        CircuitBreaker circuitBreaker = CircuitBreakerFactory.createDefaultCircuitBreaker(restyCommand.getServiceName());
        // RestyCommand ready to start
        restyCommand.ready(circuitBreaker);
        ServerInstance serverInstance = null;

        // 排除 彻底断路的server， 尝试过的server
        // 1.判断command使用的serverInstanceList是否存在被熔断的server
        // 1.1 存在的话 server加入 loadBalance 的excludeServerList
        Set<String> excludeInstanceIdSet = circuitBreaker.getBrokenServer();

        // 负载均衡器 选择可用服务实例
        serverInstance = lb.choose(serverContext, restyCommand, excludeInstanceIdSet);
        if (serverInstance == null) {
            throw new RuntimeException("no instances found:" + restyCommand.getServiceName() + ":" + serverContext.getServerList(restyCommand.getServiceName()));
        }

        // 重试机制
        for (int times = 0; times <= retry; times++) {
            try {
                boolean shouldPass = circuitBreaker.shouldPass(restyCommand, serverInstance);

                if (!shouldPass) {
                    // fallback or exception
                    throw new CircuitBreakException("circuit breaker is open");
                }

                RestyFuture future = restyCommand.start(serverInstance);

                // 同步调用
                Response response = future.get();
                result = ResponseConverterContext.DEFAULT.convertResponse(restyCommand, response);
                if (restyCommand.getStatus() == RestyCommandStatus.FAILED) {
                    throw restyCommand.getFailException();
                }

                if (restyCommand.getStatus() == RestyCommandStatus.SUCCESS) {
                    // 响应成功，无需重试了
                    break;
                }
            } catch (Exception ex) {
                if (times == retry) {
                    throw ex;
                } else {
                    // 将本次使用的server 加入排除列表
                    if (excludeInstanceIdSet == null || excludeInstanceIdSet == Collections.EMPTY_SET) {
                        excludeInstanceIdSet = new HashSet<>();
                    }

                    if (serverInstance != null) {
                        excludeInstanceIdSet.add(serverInstance.getInstanceId());
                    }
                    //选择server， 如果没有server可选则无需重试，直接抛出当前异常
                    serverInstance = lb.choose(serverContext, restyCommand, excludeInstanceIdSet);
                    if (serverInstance == null) {
                        throw ex;
                    }
                }
            }
        }
        return result;
    }


}
