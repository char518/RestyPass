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

    RestyCommandContext getRestyCommandContext();

    ServerContext getServerContext();

    CommandExecutor getCommandExecutor();

//    LoadBalancer getDefaultLoadBalancer();
    FallbackExecutor getFallbackExecutor();

    List<CommandFilter> getCommandFilter();
}
