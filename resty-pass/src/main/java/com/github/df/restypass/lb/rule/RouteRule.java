package com.github.df.restypass.lb.rule;

/**
 * 路由规则接口
 * Created by darrenfu on 17-8-27.
 */
public interface RouteRule<T> {


    boolean match(T t);

}
