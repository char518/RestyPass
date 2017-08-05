package com.github.df.restypass.spring.proxy;

import com.github.df.restypass.command.DefaultRestyCommand;
import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.exception.RestyException;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.lb.LoadBalanceFactory;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.ServerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 代理类执行器
 *
 * @author darren-fu
 * @version 1.0.0
 * @contact 13914793391
 * @date 2016/11/22
 */
@Slf4j
public class RestyProxyInvokeHandler implements InvocationHandler {

    private RestyCommandContext restyCommandContext;

    private CommandExecutor commandExecutor;

    private FallbackExecutor fallbackExecutor;

    private ServerContext serverContext;

    public RestyProxyInvokeHandler(RestyCommandContext restyCommandContext,
                                   CommandExecutor commandExecutor,
                                   FallbackExecutor fallbackExecutor,
                                   ServerContext serverContext) {
        this.restyCommandContext = restyCommandContext;
        this.commandExecutor = commandExecutor;
        this.fallbackExecutor = fallbackExecutor;
        this.serverContext = serverContext;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isSpecialMethod(method)) {
            return handleSpecialMethod(proxy, method, args);
        }

        Object result;

        RestyCommand restyCommand = new DefaultRestyCommand(method.getDeclaringClass(),
                method,
                method.getGenericReturnType(),
                args,
                restyCommandContext);

        LoadBalancer loadBalancer = LoadBalanceFactory.createLoadBalancerForService(restyCommand.getServiceName(),
                restyCommand.getRestyCommandConfig().getLoadBalancer());

        commandExecutor.setServerContext(serverContext);
        try {
            if (commandExecutor.executable(restyCommand)) {
                result = commandExecutor.execute(loadBalancer, restyCommand);
            } else {
                throw new IllegalStateException("Resty command is suitable:" + restyCommand);
            }
        } catch (RestyException ex) {
            log.warn("请求发生异常:", ex);
            if (fallbackExecutor.executable(restyCommand)) {
                result = fallbackExecutor.execute(restyCommand);
            } else {
                throw ex;
            }
        }
        return result;
    }

    private boolean isSpecialMethod(Method method) {
        return "equals".equals(method.getName())
                || "hashCode".equals(method.getName())
                || "toString".equals(method.getName());
    }

    private Object handleSpecialMethod(Object proxy, Method method, Object[] args) {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler =
                        args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(method.getName())) {
            return hashCode();
        } else if ("toString".equals(method.getName())) {
            return toString();
        }
        return null;

    }

}
