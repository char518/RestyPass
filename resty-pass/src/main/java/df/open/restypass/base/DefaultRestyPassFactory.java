package df.open.restypass.base;

import df.open.restypass.command.RestyCommandContext;
import df.open.restypass.executor.CommandExecutor;
import df.open.restypass.executor.FallbackExecutor;
import df.open.restypass.executor.RestyCommandExecutor;
import df.open.restypass.executor.RestyFallbackExecutor;
import df.open.restypass.lb.LoadBalancer;
import df.open.restypass.lb.RandomLoadBalancer;
import df.open.restypass.lb.server.CloudDiscoveryServerContext;
import df.open.restypass.lb.server.ConfigurableServerContext;
import df.open.restypass.lb.server.ServerContext;
import df.open.restypass.util.ClassTools;

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
