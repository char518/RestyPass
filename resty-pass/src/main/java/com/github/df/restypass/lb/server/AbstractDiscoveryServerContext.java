package com.github.df.restypass.lb.server;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * SpringCloud服务发现抽象容器
 * Created by darrenfu on 17-8-17.
 */
@Slf4j
public abstract class AbstractDiscoveryServerContext implements ServerContext {


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
    private void startUpdateTask() {
        if (taskStarted.compareAndSet(false, true)) {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
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
        this.updatedInstancesMap = new ConcurrentHashMap<>();
        for (String service : services) {
            updatedInstancesMap.put(service, getServiceInstances(service));
        }
        if (log.isDebugEnabled()) {
            log.debug("更新DiscoverClient server:{}", updatedInstancesMap);
        }
    }


    protected abstract boolean isDiscoveryEnabled();

    protected abstract List<String> getServiceNames();

    protected abstract List<ServerInstance> getServiceInstances(String serviceName);


}
