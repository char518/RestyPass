package com.github.df.restypass.enums;

/**
 * 过滤器类型
 * Created by darrenfu on 17-8-5.
 */
public enum CommandFilterType {
    /**
     * command 执行前过滤
     */
    BEFOR_EXECUTE(),
    // TODO AFTER 和 ERROR过滤器
    /**
     * command 执行后过滤
     */
    AFTER_EXECUTE(),

    /**
     * command执行异常后过滤
     */
    ERROR_EXECUTE()

}
