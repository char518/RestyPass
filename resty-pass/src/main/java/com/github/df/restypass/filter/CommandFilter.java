package com.github.df.restypass.filter;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.enums.CommandFilterType;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.exception.filter.FilterException;

/**
 * RestyCommand 过滤器
 * Created by darrenfu on 17-8-5.
 */
public interface CommandFilter {


    /**
     * 执行顺序，数字越小，优先级越高
     *
     * @return the int
     */
    int order();

    /**
     * Should filter boolean.
     *
     * @param restyCommand the resty command
     * @return the boolean
     */
    boolean shouldFilter(RestyCommand restyCommand);

    /**
     * 过滤器类型
     *
     * @return the before type
     */
    CommandFilterType getFilterType();

    /**
     * 过滤RestyCommand
     *
     * @param restyCommand the resty command
     * @return the resty command
     * @throws FilterException the filter exception
     */
    default void before(RestyCommand restyCommand) throws FilterException {

    }


    /**
     * After.
     *
     * @param restyCommand the resty command
     * @param result       the result
     */
    default void after(RestyCommand restyCommand, Object result) {

    }

    /**
     * Error.
     *
     * @param restyCommand the resty command
     * @param ex           the ex
     */
    default void error(RestyCommand restyCommand, RestyException ex) {

    }

}
