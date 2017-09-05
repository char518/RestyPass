package com.github.df.restypass.executor;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.exception.execute.FallbackException;

/**
 * 降级服务执行器
 * Created by darrenfu on 17-7-28.
 */
public interface FallbackExecutor {

    /**
     * Executable boolean.
     *
     * @param restyCommand the resty command
     * @return the boolean
     */
    boolean executable(RestyCommand restyCommand);

    /**
     * 执行RestyCommand降级服务，返回结果
     *
     * @param restyCommand the resty command
     * @return the t
     * @throws FallbackException 执行降级发生的异常
     */
    Object execute(RestyCommand restyCommand) throws FallbackException;
}
