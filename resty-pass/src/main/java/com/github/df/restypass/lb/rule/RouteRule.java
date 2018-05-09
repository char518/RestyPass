package com.github.df.restypass.lb.rule;

/**
 * 路由规则接口
 *
 * @author darrenfu
 * @date 17-8-27
 *
 * @param <T> the type parameter
 */
public interface RouteRule<T> {


    /**
     * Match boolean.
     *
     * @param t the t
     * @return the boolean
     */
    boolean match(T t);

}
