package com.github.df.restypass.lb;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 负载均衡器工厂类
 * Created by darrenfu on 17-6-28.
 */
@Slf4j
public class LoadBalanceFactory {

    private static final ConcurrentHashMap<String, LoadBalancer> serviceLoadBalancerMap = new ConcurrentHashMap<>();


    /**
     * 按照service提供LB
     *
     * @param serviceName
     * @param loadBalancer
     * @return
     */
    public static LoadBalancer createLoadBalancerForService(String serviceName, String loadBalancer) {

        LoadBalancer balancer = serviceLoadBalancerMap.get(serviceName);
        if (balancer == null) {
            LoadBalancer newLoadBalancer = createLoadBalancer(loadBalancer);
            serviceLoadBalancerMap.putIfAbsent(serviceName, newLoadBalancer);
            balancer = serviceLoadBalancerMap.get(serviceName);
        }

        return balancer;
    }

    private static LoadBalancer createLoadBalancer(String loadBalancer) {
        if (RandomLoadBalancer.NAME.equalsIgnoreCase(loadBalancer)) {
            return createRandomLoadBalancer();
        } else if (RoundRobinLoadBalancer.NAME.equalsIgnoreCase(loadBalancer)) {
            return createRoundRobinBalancer();
        } else if (loadBalancer.contains(".")) {
            try {
                Class<?> loadBalancerClz = Class.forName(loadBalancer);
                return (LoadBalancer) loadBalancerClz.newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
                log.error("无法创建指定的负载均衡器:{},ex:{} ", loadBalancer, e.getMessage(), e);
            }
        }
        return createRoundRobinBalancer();
    }

    /**
     * Create random load balancer load balancer.
     *
     * @return the load balancer
     */
    public static LoadBalancer createRandomLoadBalancer() {
        return new RandomLoadBalancer();
    }

    public static LoadBalancer createRoundRobinBalancer() {
        return new RoundRobinLoadBalancer();
    }


}
