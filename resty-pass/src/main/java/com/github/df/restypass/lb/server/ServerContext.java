package com.github.df.restypass.lb.server;

import java.util.List;

/**
 * 服务实例上下文
 * Created by darrenfu on 17-6-26.
 */
public interface ServerContext {


    /**
     * 获取所有service name
     *
     * @return the all service name
     */
    List<String> getAllServiceName();

    /**
     * 获取所有服务实例列表
     *
     * @return the all server list
     */
    List<ServerInstance> getAllServerList();

    /**
     * 获取指定serviceName下的服务实例列表
     *
     * @param serviceName the service name
     * @return the server list
     */
    List<ServerInstance> getServerList(String serviceName);

    /**
     * 刷新所有服务实例
     */
    void refreshServerList();

    /**
     * 刷新serviceName下的服务实例
     *
     * @param serviceName the service name
     */
    void refreshServerList(String serviceName);


    /**
     * Sets server list.
     *
     * @param instanceList the instance list
     * @return the server list
     */
    List<ServerInstance> setServerList(List<ServerInstance> instanceList);

    /**
     * Add server list list.
     *
     * @param instanceList the instance list
     * @return the list
     */
    List<ServerInstance> addServerList(List<ServerInstance> instanceList);

}
