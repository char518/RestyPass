package com.github.df.restypass.command;

import com.github.df.restypass.base.RestyPassFactory;
import com.github.df.restypass.command.update.UpdateCommandConfig;
import com.github.df.restypass.command.update.Updater;
import com.github.df.restypass.lb.server.VersionRule;

import java.util.List;

/**
 * Resty请求的配置
 * Created by darrenfu on 17-6-21.
 */
@SuppressWarnings("unused")
public interface RestyCommandConfig extends Updater<UpdateCommandConfig> {

    /**
     * 获取服务名称
     *
     * @return the service name
     */
    String getServiceName();

    /**
     * 设置服务名称
     *
     * @param serviceName the service name
     */
    void setServiceName(String serviceName);


    /**
     * 断路器是否启用
     * 启用时在达到一定条件时，断路器会自动熔断服务接口
     *
     * @return the boolean
     */
    boolean isCircuitBreakEnabled();

    /**
     * 是否强制熔断
     * <p>
     * true:强制熔断服务，直到设置为false
     *
     * @return the boolean
     */
    boolean isForceBreakEnabled();

    /**
     * 是否启用降级服务
     * true:启用降级服务，熔断后自动调用降级服务并返回结果
     *
     * @return the boolean
     */
    boolean isFallbackEnabled();

    /**
     * 降级服务Class
     *
     * @return the fallback class
     */
    Class getFallbackClass();

    /**
     * 降级服务Bean
     *
     * @return the fallback
     */
    String getFallbackBean();

    /**
     * 重试次数
     *
     * @return the retry
     */
    int getRetry();

    /**
     * 配置使用的负载均衡器
     *
     * @return the load balancer
     */
    String getLoadBalancer();

    /**
     * 限流
     *
     * @return the limit
     */
    int getLimit();

    /**
     * 基础工厂类
     *
     * @return the factory
     */
    Class<? extends RestyPassFactory> getFactory();

    /**
     * 版本控制
     *
     * @return the version
     */
    List<VersionRule> getVersion();

    /**
     * Sets circuit break enabled.
     *
     * @param enableCircuitBreak the enable circuit break
     */
    void setCircuitBreakEnabled(boolean enableCircuitBreak);

    /**
     * Sets force break enabled.
     *
     * @param forceBreakEnabled the force break enabled
     */
    void setForceBreakEnabled(boolean forceBreakEnabled);

    /**
     * Sets fallback enabled.
     *
     * @param enableFallback the enable fallback
     */
    void setFallbackEnabled(boolean enableFallback);

    /**
     * Sets fallback class.
     *
     * @param fallbackClass the fallback class
     */
    void setFallbackClass(Class fallbackClass);

    /**
     * Sets fallback bean.
     *
     * @param fallbackBean the fallback bean
     */
    void setFallbackBean(String fallbackBean);

    /**
     * Sets retry.
     *
     * @param retry the retry
     */
    void setRetry(int retry);


    /**
     * Sets load balancer.
     *
     * @param loadBalancer the load balancer
     */
    void setLoadBalancer(String loadBalancer);

    /**
     * Sets limit.
     *
     * @param limit the limit
     */
    void setLimit(int limit);

    /**
     * Sets factory.
     *
     * @param factoryClz the factory clz
     */
    void setFactory(Class<? extends RestyPassFactory> factoryClz);

    /**
     * Sets version.
     *
     * @param version the version
     */
    void setVersion(List<VersionRule> version);


    /**
     * The type Default resty command config.
     */
    class DefaultRestyCommandConfig implements RestyCommandConfig {

        private String serviceName;

        private boolean circuitBreakEnabled = true;

        private boolean forceBreakEnabled = false;

        private boolean fallbackEnabled = true;

        private Class fallbackClass;

        private String fallbackBean;

        private int retry = 1;

        private String loadBalancer;

        private int limit = -1;

        private List<VersionRule> version;

        private Class<? extends RestyPassFactory> factory;

        @Override
        public String getServiceName() {
            return serviceName;
        }

        @Override
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }


        @Override
        public boolean isCircuitBreakEnabled() {
            return circuitBreakEnabled;
        }

        @Override
        public boolean isFallbackEnabled() {
            return fallbackEnabled;
        }

        @Override
        public Class getFallbackClass() {
            return fallbackClass;
        }

        @Override
        public String getFallbackBean() {
            return fallbackBean;
        }

        @Override
        public int getRetry() {
            return retry;
        }

        @Override
        public List<VersionRule> getVersion() {
            return version;
        }


        @Override
        public void setCircuitBreakEnabled(boolean enableCircuitBreak) {
            this.circuitBreakEnabled = enableCircuitBreak;
        }

        @Override
        public void setFallbackEnabled(boolean enableFallback) {
            this.fallbackEnabled = enableFallback;
        }

        @Override
        public void setFallbackClass(Class fallbackClass) {
            this.fallbackClass = fallbackClass;
        }

        @Override
        public void setFallbackBean(String fallbackBean) {
            this.fallbackBean = fallbackBean;
        }

        @Override
        public void setRetry(int retry) {
            this.retry = retry;
        }

        @Override
        public boolean isForceBreakEnabled() {
            return forceBreakEnabled;
        }

        @Override
        public void setForceBreakEnabled(boolean forceBreakEnabled) {
            this.forceBreakEnabled = forceBreakEnabled;
        }

        @Override
        public String getLoadBalancer() {
            return loadBalancer;
        }

        @Override
        public void setLoadBalancer(String loadBalancer) {
            this.loadBalancer = loadBalancer;
        }


        @Override
        public int getLimit() {
            return limit;
        }

        @Override
        public void setLimit(int limit) {
            this.limit = limit;
        }

        @Override
        public Class<? extends RestyPassFactory> getFactory() {
            return factory;
        }

        @Override
        public void setFactory(Class<? extends RestyPassFactory> factory) {
            this.factory = factory;
        }

        @Override
        public void setVersion(List<VersionRule> version) {
            this.version = version;
        }

        @Override
        public boolean refresh(UpdateCommandConfig updateCommandConfig) {

            if (updateCommandConfig.getCircuitBreakEnabled() != null) {
                this.setCircuitBreakEnabled(updateCommandConfig.getCircuitBreakEnabled());
            }
            if (updateCommandConfig.getFallbackEnabled() != null) {
                this.setFallbackEnabled(updateCommandConfig.getFallbackEnabled());
            }
            return true;
        }
    }
}
