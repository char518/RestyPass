package com.github.df.restypass.command;

import com.github.df.restypass.base.RestyPassFactory;
import com.github.df.restypass.command.update.UpdateCommandConfig;
import com.github.df.restypass.command.update.Updater;

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
     * Is async enabled boolean.
     *
     * @return the boolean
     */
    boolean isAsyncEnabled();

    /**
     * Is circuit break enabled boolean.
     *
     * @return the boolean
     */
    boolean isCircuitBreakEnabled();

    /**
     * Is force break enabled boolean.
     *
     * @return the boolean
     */
    boolean isForceBreakEnabled();

    /**
     * Is fallback enabled boolean.
     *
     * @return the boolean
     */
    boolean isFallbackEnabled();

    /**
     * Gets fallback class.
     *
     * @return the fallback class
     */
    Class getFallbackClass();

    /**
     * Gets fallback com.github.df.restypass.servertest.entity.
     *
     * @return the fallback com.github.df.restypass.servertest.entity
     */
    String getFallbackBean();

    /**
     * Gets retry.
     *
     * @return the retry
     */
    int getRetry();

    /**
     * Gets load balancer.
     *
     * @return the load balancer
     */
    String getLoadBalancer();

    /**
     * Gets limit.
     *
     * @return the limit
     */
    int getLimit();

    /**
     * Gets factory.
     *
     * @return the factory
     */
    Class<? extends RestyPassFactory> getFactory();

    String getVersion();

    /**
     * Sets async enabled.
     *
     * @param asyncEnabled the async enabled
     */
    void setAsyncEnabled(boolean asyncEnabled);

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
     * Sets fallback com.github.df.restypass.servertest.entity.
     *
     * @param fallbackBean the fallback com.github.df.restypass.servertest.entity
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

    void setVersion(String version);


    /**
     * The type Default resty command config.
     */
    class DefaultRestyCommandConfig implements RestyCommandConfig {

        private String serviceName;

        private boolean asyncEnabled = false;

        private boolean circuitBreakEnabled = true;

        private boolean forceBreakEnabled = false;

        private boolean fallbackEnabled = true;

        private Class fallbackClass;

        private String fallbackBean;

        private int retry = 1;

        private String loadBalancer;

        private int limit = -1;
        
        private String version;

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
        public boolean isAsyncEnabled() {
            return asyncEnabled;
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
        public String getVersion() {
            return version;
        }

        @Override
        public void setAsyncEnabled(boolean asyncEnabled) {
            this.asyncEnabled = asyncEnabled;
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
        public void setVersion(String version) {
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
