package com.github.df.restypass.lb.server;

import com.github.df.restypass.base.RestyConst;
import com.github.df.restypass.util.ClassTools;
import com.github.df.restypass.util.DateFormatTools;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Spring Cloud DiscoveryClient 服务容器
 * Created by darrenfu on 17-8-1.
 */
@Slf4j
public class CloudDiscoveryServerContext extends AbstractDiscoveryServerContext implements ApplicationContextAware {

    private ReentrantLock initLock = new ReentrantLock();

    private DiscoveryClient discoveryClient;

    private ApplicationContext applicationContext;

    /**
     * Spring Cloud 服务发现是否启用
     */
    @Getter
    private AtomicBoolean cloudyDiscoveryEnabled = new AtomicBoolean(false);

    /**
     * 转换数据格式
     *
     * @param serviceInstanceList
     * @return
     */
    protected List<ServerInstance> convertToServerInstanceList(List<ServiceInstance> serviceInstanceList) {
        if (serviceInstanceList != null) {
            return serviceInstanceList.stream().map(v -> convertToServerInstance(v)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    private ServerInstance convertToServerInstance(ServiceInstance serviceInstance) {
        ServerInstance instance = new ServerInstance();

        instance.setServiceName(serviceInstance.getServiceId());
        instance.setIsHttps(serviceInstance.isSecure());
        instance.setHost(serviceInstance.getHost());
        instance.setPort(serviceInstance.getPort());

        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null && metadata.size() > 0) {
            Map<String, Object> props = new HashMap();

            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                props.put(entry.getKey(), entry.getValue());
                // 服务启动的时间
                if (RestyConst.Instance.PROP_TIMESTAMP_KEY.equalsIgnoreCase(entry.getKey())
                        && StringUtils.isNotEmpty(entry.getValue())) {
                    instance.setStartTime(DateFormatTools.parseDate(entry.getValue()));
                }
            }
            instance.setProps(props);
        }
        instance.initProps();
        return instance;
    }

    /**
     * 获取DiscoveryClient
     *
     * @return
     */
    private DiscoveryClient getClient() {

        if (this.discoveryClient != null) {
            return this.discoveryClient;
        }
        initLock.lock();
        try {
            DiscoveryClient discoveryClient = this.applicationContext.getBean(DiscoveryClient.class);
            this.discoveryClient = discoveryClient;
        } catch (Exception ex) {
            throw new RuntimeException("没有发现bean实例:" + DiscoveryClient.class.getSimpleName());
        } finally {
            initLock.unlock();
        }
        return this.discoveryClient;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        init(applicationContext);
    }

    protected void init(ApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        if (isCloudConsulEnabled(env) || isCloudEurekaEnabled(env) || isCloudZookeeperEnabled(env)) {
            cloudyDiscoveryEnabled.compareAndSet(false, true);
        }
    }


    protected boolean isCloudConsulEnabled(Environment env) {
        boolean isUseCloudConsul = ClassTools.hasClass("org.springframework.cloud.consul.discovery.ConsulDiscoveryClient");
        return isUseCloudConsul && "true".equalsIgnoreCase(env.getProperty("spring.cloud.consul.discovery.enabled", "true"));
    }

    protected boolean isCloudZookeeperEnabled(Environment env) {
        boolean isUseCloudZookeeper = ClassTools.hasClass("org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient");
        return isUseCloudZookeeper && "true".equalsIgnoreCase(env.getProperty("spring.cloud.zookeeper.discovery.enabled", "true"));
    }

    protected boolean isCloudEurekaEnabled(Environment env) {
        boolean isUseCloudEureka = ClassTools.hasClass("org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient");
        return isUseCloudEureka && "true".equalsIgnoreCase(env.getProperty("eureka.client.fetchRegistry", "true"));
    }


    @Override
    protected boolean isDiscoveryEnabled() {
        return cloudyDiscoveryEnabled.get();
    }

    @Override
    protected List<String> getServiceNames() {
        return getClient().getServices();
    }

    @Override
    protected List<ServerInstance> getServiceInstances(String serviceName) {
        return convertToServerInstanceList(getClient().getInstances(serviceName));
    }
}
