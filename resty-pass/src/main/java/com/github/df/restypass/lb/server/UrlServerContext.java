package com.github.df.restypass.lb.server;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * URL服务容器
 * Created by darrenfu on 18-3-20.
 *
 * @author: darrenfu
 * @date: 18-3-20
 */
public class UrlServerContext implements ServerContext {

    private static final Logger log = LoggerFactory.getLogger(UrlServerContext.class);
    private volatile ConcurrentHashMap<String, List<ServerInstance>> instancesMap = new ConcurrentHashMap<>();


    public static UrlServerContext getInstance() {
        return UrlServerContextHolder.urlServerContext;
    }


    private static class UrlServerContextHolder {
        private static UrlServerContext urlServerContext = new UrlServerContext();
    }


    public void addUrl(String serviceName, String[] urls) {
        if (urls != null && urls.length > 0) {
            List<ServerInstance> instanceList = Stream.of(urls).map(url -> {
                ServerInstance instance = new ServerInstance();
                instance.setServiceName(serviceName);
                parserURL(url, instance);
                instance.ready();
                return instance;
            }).collect(Collectors.toList());
            instancesMap.put(serviceName, instanceList);
            log.info("{}解析URL服务地址:{}", serviceName, ArrayUtils.toString(instanceList));
        }


    }

    /**
     * Parser url server instance.
     *
     * @param url      the url
     * @param instance the instance
     * @return the server instance
     */
    protected ServerInstance parserURL(String url, ServerInstance instance) {

        try {
            URL result = new URL(url);
            instance.setUserInfo(result.getUserInfo());
            instance.setHost(result.getHost());
            instance.setPort(result.getPort() <= 0 ? result.getDefaultPort() : result.getPort());
            instance.setIsHttps("https".equalsIgnoreCase(result.getProtocol()) ? true : false);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return instance;
    }


    @Override
    public List<String> getAllServiceName() {
        return instancesMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public List<ServerInstance> getAllServerList() {
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
        return instancesMap.getOrDefault(serviceName, Collections.EMPTY_LIST);
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
}
