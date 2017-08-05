package com.github.df.restypass.command.update;

/**
 * 配置更新接口
 * Created by darrenfu on 17-7-27.
 *
 * @param <T> the type parameter
 */
public interface Updater<T> {

    /**
     * Refresh
     *
     * @param t the t
     * @return the boolean
     */
    boolean refresh(T t);
}
