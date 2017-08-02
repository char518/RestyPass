package df.open.restypass.base;

import df.open.restypass.command.RestyCommandContext;
import df.open.restypass.executor.CommandExecutor;
import df.open.restypass.executor.FallbackExecutor;
import df.open.restypass.lb.server.ServerContext;

/**
 * 工厂接口
 * Created by darrenfu on 17-7-27.
 */
public interface RestyPassFactory {

    RestyCommandContext getRestyCommandContext();

    ServerContext getServerContext();

    CommandExecutor getCommandExecutor();

//    LoadBalancer getDefaultLoadBalancer();
    FallbackExecutor getFallbackExecutor();
}
