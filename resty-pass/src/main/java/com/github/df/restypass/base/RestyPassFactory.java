package com.github.df.restypass.base;

import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.filter.CommandFilter;
import com.github.df.restypass.lb.server.ServerContext;

import java.util.List;

/**
 * 工厂接口
 * Created by darrenfu on 17-7-27.
 */
public interface RestyPassFactory {

    /**
     * Gets resty command context.
     *
     * @return the resty command context
     */
    RestyCommandContext getRestyCommandContext();

    /**
     * Gets server context.
     *
     * @return the server context
     */
    ServerContext getServerContext();

    /**
     * Gets command executor.
     *
     * @return the command executor
     */
    CommandExecutor getCommandExecutor();

    /**
     * Gets fallback executor.
     *
     * @return the fallback executor
     */
//    LoadBalancer getDefaultLoadBalancer();
    FallbackExecutor getFallbackExecutor();

    /**
     * Gets command filter.
     *
     * @return the command filter
     */
    List<CommandFilter> getCommandFilter();
}
