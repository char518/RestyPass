package com.github.df.restypass.base;

import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.executor.RestyCommandExecutor;
import com.github.df.restypass.executor.RestyFallbackExecutor;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.CloudDiscoveryServerContext;
import com.github.df.restypass.lb.server.ConfigurableServerContext;
import com.github.df.restypass.lb.server.ServerContext;
import com.github.df.restypass.util.ClassTools;

/**
 * 默认工厂类
 * Created by darrenfu on 17-7-27.
 */
public class DefaultRestyPassFactory implements RestyPassFactory {
    public static RestyPassFactory INSTANCE = new DefaultRestyPassFactory();

    @Override
    public RestyCommandContext getRestyCommandContext() {
        return RestyCommandContext.getInstance();
    }

    @Override
    public ServerContext getServerContext() {
        boolean hasClass = ClassTools.hasClass("org.springframework.cloud.client.discovery.DiscoveryClient");
        if (hasClass) {
            return new CloudDiscoveryServerContext();
        } else {
            return new ConfigurableServerContext();
        }
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return new RestyCommandExecutor(getRestyCommandContext());
    }

    @Override
    public FallbackExecutor getFallbackExecutor() {
        return new RestyFallbackExecutor();
    }


    public static <T> T getDefaultBean(Class<T> clz) {
        if (clz.equals(ServerContext.class)) {
            return (T) INSTANCE.getServerContext();
        } else if (clz.equals(CommandExecutor.class)) {
            return (T) INSTANCE.getCommandExecutor();
        } else if (clz.equals(LoadBalancer.class)) {
            // loadbalancer使用注解单独配置
//            return (T) INSTANCE.getDefaultLoadBalancer();
        } else if (clz.equals(FallbackExecutor.class)) {
            return (T) INSTANCE.getFallbackExecutor();
        }

        return null;
    }
}
