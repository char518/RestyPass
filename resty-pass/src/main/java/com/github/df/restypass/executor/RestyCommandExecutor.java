package com.github.df.restypass.executor;

import com.github.df.restypass.cb.CircuitBreaker;
import com.github.df.restypass.cb.CircuitBreakerFactory;
import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.command.RestyFuture;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.CircuitBreakException;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.ServerContext;
import com.github.df.restypass.lb.server.ServerInstance;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * 异步Resty请求执行器
 * Created by darrenfu on 17-7-1.
 */
@SuppressWarnings("unused")
public class RestyCommandExecutor implements CommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(RestyCommandExecutor.class);

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
    public Object execute(LoadBalancer lb, RestyCommand restyCommand) throws RestyException{

        if (restyCommand.getRestyCommandConfig().isForceBreakEnabled()) {
            throw new CircuitBreakException("circuit breaker is forced to open");
        }

        // 重试次数
        int retry = restyCommand.getRestyCommandConfig().getRetry();
        Object result = null;
        CircuitBreaker circuitBreaker = CircuitBreakerFactory.createDefaultCircuitBreaker(restyCommand.getServiceName());
        // RestyCommand ready to start
        restyCommand.ready(circuitBreaker);

        // 排除 彻底断路的server， 尝试过的server
        // 1.判断command使用的serverInstanceList是否存在被熔断的server
        // 1.1 存在的话 server加入 loadBalance 的excludeServerList
        Set<String> excludeInstanceIdSet = circuitBreaker.getBrokenServer();

        // 负载均衡器 选择可用服务实例
        ServerInstance serverInstance = lb.choose(serverContext, restyCommand, excludeInstanceIdSet);
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
                RestyFuture futureArg = getFutureArg(restyCommand);


                if (restyCommand.isAsyncArg()) {
                    //异步请求，请求参数中包含RestyFuture作为出参,
                    //直接返回null， 结果放入出参RestyFuture中
                    return null;
                }

                if (restyCommand.isAsyncReturn()) {
                    //异步请求名，请求返回类型是Future<~>
                    //返回RestyFuture
                    return future;
                }

                // 同步调用
                result = future.get();
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
                    if (serverInstance == null || excludeInstanceIdSet.contains(serverInstance.getInstanceId())) {
                        throw ex;
                    } else if (log.isDebugEnabled()) {
                        log.debug("请求切换服务实例重试:{}", serverInstance);
                    }
                }
            }
        }
        return result;
    }


    /**
     * 获取请求中的RestyFuture参数
     *
     * @param restyCommand
     * @return
     */
    private RestyFuture getFutureArg(RestyCommand restyCommand) {
        if (restyCommand.getArgs() != null && restyCommand.getArgs().length > 0) {
            for (Object o : restyCommand.getArgs()) {
                if (o instanceof RestyFuture) {
                    return (RestyFuture) o;
                }
            }
        }
        return null;
    }


}
