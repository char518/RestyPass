package com.github.df.restypass.executor;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.lb.LoadBalancer;
import com.github.df.restypass.lb.server.ServerContext;

/**
 * 执行器
 * Created by darrenfu on 17-7-1.
 */
@SuppressWarnings("unused")
public interface CommandExecutor {

    /**
     * Executable boolean.
     *
     * @param restyCommand the resty command
     * @return the boolean
     */
    boolean executable(RestyCommand restyCommand);

    /**
     * 执行RestyCommand，返回结果
     *
     * @param lb           the lb
     * @param restyCommand the resty command
     * @return the t
     * @throws RestyException the resty exception
     */
    Object execute(LoadBalancer lb, RestyCommand restyCommand) throws RestyException;


    /**
     * Sets server context.
     *
     * @param serverContext the server context
     */
    void setServerContext(ServerContext serverContext);
}
