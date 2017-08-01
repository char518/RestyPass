package df.open.restypass.lb.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Spring Cloud DiscoveryClient 服务容器
 * Created by darrenfu on 17-8-1.
 */
@Slf4j
public class CloudDiscoveryServerContext implements ServerContext, ApplicationContextAware {

    private DiscoveryClient discoveryClient;
    private ApplicationContext applicationContext;

    private ReentrantLock initLock = new ReentrantLock();


    @Override
    public List<String> getAllServiceName() {
        return getClient().getServices();
    }

    @Override
    public List<ServerInstance> getAllServerList() {
        return null;
    }

    @Override
    public List<ServerInstance> getServerList(String serviceName) {
        List<ServiceInstance> instances = getClient().getInstances(serviceName);
        List<ServerInstance> serverInstances = convertToServerInstanceList(instances);
        return serverInstances;
    }

    @Override
    public void refreshServerList() {

    }

    @Override
    public void refreshServerList(String serviceName) {

    }

    @Override
    public List<ServerInstance> setServerList(List<ServerInstance> instanceList) {
        return null;
    }

    @Override
    public List<ServerInstance> addServerList(List<ServerInstance> instanceList) {
        return null;
    }


    private List<ServerInstance> convertToServerInstanceList(List<ServiceInstance> serviceInstanceList) {
        if (serviceInstanceList != null) {
            return serviceInstanceList.stream().map(v -> convertToServerInstance(v)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
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
            metadata.forEach((k, v) -> props.put(k, v));
            instance.setProps(props);
        }
        instance.init();
        return instance;
    }

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
    }
}
