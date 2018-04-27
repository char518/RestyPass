package com.github.df.restypass.lb.server;

import com.github.df.restypass.util.CommonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * SpringCloud服务发现抽象容器
 *
 * @author darrenfu
 * @date 17-8-17
 */
public abstract class AbstractDiscoveryServerContext implements ServerContext {

    private static final Logger log = LoggerFactory.getLogger(AbstractDiscoveryServerContext.class);

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


    /**
     * update task是否启动
     */
    private AtomicBoolean taskStarted = new AtomicBoolean(false);

    /**
     * Instantiates a new Abstract discovery server context.
     */
    public AbstractDiscoveryServerContext() {
        startUpdateTask();
    }


    @Override
    public List<String> getAllServiceName() {
        return instancesMap.keySet().stream().collect(Collectors.toList());
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
        throw new UnsupportedOperationException("基于SpringCloud自动服务发现，不支持手动设置服务实例");
    }

    @Override
    public List<ServerInstance> addServerList(List<ServerInstance> instanceList) {
        throw new UnsupportedOperationException("基于SpringCloud自动服务发现，不支持手动设置服务实例");
    }


    /**
     * 检查server是否更新
     */
    protected void checkStatus() {
        if (updated.compareAndSet(true, false)) {
            this.instancesMap = updatedInstancesMap;
        }
    }


    /**
     * 定时任务
     */
    @SuppressWarnings("AlibabaThreadPoolCreation")
    private void startUpdateTask() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "server-discover-thread-" + counter++);
            }
        });
        if (taskStarted.compareAndSet(false, true)) {
            executor.scheduleWithFixedDelay(() -> {
                try {
                    if (isDiscoveryEnabled()) {
                        updated.set(false);
                        updateServer();
                        updated.set(true);
                    }
                } catch (Exception ex) {
                    log.warn("更新server发生错误:" + ex.getMessage(), ex);
                }

            }, 3 * 1000, 20 * 1000, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * 更新server
     */
    protected void updateServer() {
        List<String> services = getServiceNames();
        this.updatedInstancesMap = new ConcurrentHashMap<>(32);
        for (String service : services) {
            try {
                List<ServerInstance> instances = Collections.EMPTY_LIST;
                // 先尝试获取URL配置的服务
                List<ServerInstance> urlInstanceList = UrlServerContext.getInstance().getServerList(service);
                if (CommonTools.isNotEmpty(urlInstanceList)) {
                    instances = UrlServerContext.getInstance().getServerList(service);
                } else {
                    instances = getServiceInstances(service);
                }
                updatedInstancesMap.put(service, instances);

            } catch (Exception ex) {
                log.warn("更新server:{}发生错误:{}", service, ex.getMessage(), ex);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("刷新server instances:{}", updatedInstancesMap);
        }
    }


    /**
     * Is discovery enabled boolean.
     *
     * @return the boolean
     */
    protected abstract boolean isDiscoveryEnabled();

    /**
     * Gets service names.
     *
     * @return the service names
     */
    protected abstract List<String> getServiceNames();

    /**
     * Gets service instances.
     *
     * @param serviceName the service name
     * @return the service instances
     */
    protected abstract List<ServerInstance> getServiceInstances(String serviceName);


}
