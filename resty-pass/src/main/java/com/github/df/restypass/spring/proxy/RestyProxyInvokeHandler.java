package com.github.df.restypass.spring.proxy;

import com.github.df.restypass.command.DefaultRestyCommand;
import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.enums.CommandFilterType;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.filter.CommandFilter;
import com.github.df.restypass.filter.CommandFilterContext;
import com.github.df.restypass.lb.LoadBalanceFactory;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.ServerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

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

    /**
     * Command容器
     */
    private RestyCommandContext restyCommandContext;

    /**
     * 命令执行器
     */
    private CommandExecutor commandExecutor;

    /**
     * 降级服务执行器
     */
    private FallbackExecutor fallbackExecutor;

    /**
     * 服务实例容器
     */
    private ServerContext serverContext;

    /**
     * 过滤器容器
     */
    private CommandFilterContext commandFilterContext;

    public RestyProxyInvokeHandler(RestyCommandContext restyCommandContext,
                                   CommandExecutor commandExecutor,
                                   FallbackExecutor fallbackExecutor,
                                   ServerContext serverContext,
                                   CommandFilterContext commandFilterContext) {
        this.restyCommandContext = restyCommandContext;
        this.commandExecutor = commandExecutor;
        this.fallbackExecutor = fallbackExecutor;
        this.serverContext = serverContext;
        this.commandFilterContext = commandFilterContext;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isSpecialMethod(method)) {
            return handleSpecialMethod(proxy, method, args);
        }

        Object result;

        //创建请求载体，RestyCommand
        RestyCommand restyCommand = new DefaultRestyCommand(method.getDeclaringClass(),
                method,
                method.getGenericReturnType(),
                args,
                restyCommandContext);
        // 获取过滤器
        List<CommandFilter> filterList = commandFilterContext.getFilterList();

        // 执行过滤器
        for (CommandFilter commandFilter : filterList) {
            if (CommandFilterType.BEFOR_EXECUTE.equals(commandFilter.getFilterType())
                    && commandFilter.shouldFilter(restyCommand)) {
                commandFilter.before(restyCommand);
            }
        }

        // 创建负载均衡器
        LoadBalancer loadBalancer = LoadBalanceFactory.createLoadBalancerForService(restyCommand.getServiceName(),
                restyCommand.getRestyCommandConfig().getLoadBalancer());

        // 为executor设置服务容器
        commandExecutor.setServerContext(serverContext);
        try {
            if (commandExecutor.executable(restyCommand)) {
                result = commandExecutor.execute(loadBalancer, restyCommand);
            } else {
                throw new IllegalStateException("Resty command is not executable:" + restyCommand);
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
