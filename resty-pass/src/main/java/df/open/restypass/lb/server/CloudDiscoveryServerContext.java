package df.open.restypass.lb.server;

import df.open.restypass.base.RestyConst;
import df.open.restypass.util.DateFormatTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    /**
     * server缓存
     */
    private volatile ConcurrentHashMap<String, List<ServerInstance>> instancesMap = new ConcurrentHashMap<>();

    /**
     * 更新的数据存储map
     */
    private volatile ConcurrentHashMap<String, List<ServerInstance>> updatedInstancesMap = new ConcurrentHashMap<>();

    /**
     * 数据是否更新的标志
     */
    private AtomicBoolean updated = new AtomicBoolean(false);

    @Override
    public List<String> getAllServiceName() {
        return getClient().getServices();
    }

    @Override
    public List<ServerInstance> getAllServerList() {
        checkStatus();
        List<ServerInstance> instances = new LinkedList<>();
        for (Map.Entry<String, List<ServerInstance>> entry : instancesMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                instances.addAll(entry.getValue());
            }
        }
        return instances;
    }

    @Override
    public List<ServerInstance> getServerList(String serviceName) {
        checkStatus();
        return instancesMap.getOrDefault(serviceName, Collections.EMPTY_LIST);
    }

    @Override
    public void refreshServerList() {
        updateServer();
    }

    @Override
    public void refreshServerList(String serviceName) {
        updateServer();
    }

    @Override
    public List<ServerInstance> setServerList(List<ServerInstance> instanceList) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public List<ServerInstance> addServerList(List<ServerInstance> instanceList) {
        throw new UnsupportedOperationException("不支持此操作");
    }


    /**
     * 检查server是否更新
     */
    private void checkStatus() {
        if (updated.get()) {
            if (updated.compareAndSet(true, false)) {
                this.instancesMap = updatedInstancesMap;
            }
        }
    }


    /**
     * 转换数据格式
     *
     * @param serviceInstanceList
     * @return
     */
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
        startUpdateTask();
    }

    /**
     * 定时任务
     */
    private void startUpdateTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            updated.set(false);
            updateServer();
            updated.set(true);
        }, 1 * 1000, 20 * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 更新server
     */
    private void updateServer() {
        List<String> services = getClient().getServices();
        this.updatedInstancesMap = new ConcurrentHashMap<>();
        for (String service : services) {
            List<ServiceInstance> instances = getClient().getInstances(service);
            updatedInstancesMap.put(service, convertToServerInstanceList(instances));
        }
        log.info("更新DiscoverClient server:{}", updatedInstancesMap);
    }
}
