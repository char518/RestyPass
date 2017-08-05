package com.github.df.restypass.command;

import com.github.df.restypass.cb.CircuitBreaker;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.lb.server.ServerInstance;

/**
 * RestyCommand生命周期
 * ready->start->success/fail
 * Created by darrenfu on 17-7-22.
 */
public interface RestyCommandLifeCycle {

    /**
     * 获取RestyCommand执行状态
     *
     * @return the boolean
     */
    RestyCommandStatus getStatus();


    /**
     * 准备状态，设置熔断器， 申请HttpClient
     *
     * @param cb the cb
     * @return the resty command life cycle
     */
    RestyCommandLifeCycle ready(CircuitBreaker cb);

    /**
     * 开始请求.
     *
     * @param instance the instance
     * @return the listenable future
     */
    RestyFuture start(ServerInstance instance);


    /**
     * Gets instance id.
     *
     * @return the instance id
     */
    String getInstanceId();

    /**
     * RestyCommand执行成功
     */
    void success();

    /**
     * RestyCommand执行失败
     *
     * @param RestyException the resty exception
     */
    void failed(RestyException RestyException);


    /**
     * 获取导致command失败异常
     *
     * @return the fail exception
     */
    RestyException getFailException();


}
