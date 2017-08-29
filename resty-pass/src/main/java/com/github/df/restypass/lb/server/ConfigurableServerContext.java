package com.github.df.restypass.lb.server;

import com.github.df.restypass.util.StreamTools;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 配置化
 * 服务容器
 * Created by darrenfu on 17-6-28.
 */
public class ConfigurableServerContext implements ServerContext {
    private static final Logger log = LoggerFactory.getLogger(ConfigurableServerContext.class);

    public static final String CONFIG_FILE_NAME = "resty-server.yaml";

    private Map<String, List<ServerInstance>> instanceMap;

    public ConfigurableServerContext() {
        this.instanceMap = new ConcurrentHashMap<>();
        loadServerFromConfigFile();
    }

    /**
     * 加载配置文件
     */
    private void loadServerFromConfigFile() {
        InputStream inputStream = parseYamlFile(CONFIG_FILE_NAME, true);
        Yaml yaml = new Yaml();
        YamlServerConfig config = yaml.loadAs(inputStream, YamlServerConfig.class);

        List<YamlServerList> servers = config.servers;
        for (YamlServerList server : servers) {
            for (ServerInstance instance : server.getInstances()) {
                instance.setServiceName(server.getServiceName());
                instance.ready();
            }
            instanceMap.put(server.getServiceName(), server.getInstances());
        }
        log.info("成功加载server的配置文件:{},Server:{}", CONFIG_FILE_NAME, instanceMap);
    }


    private InputStream parseYamlFile(String file, boolean required) {

        List<ClassLoader> cls = new ArrayList<>();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            cls.add(cl);
        }
        cl = getClass().getClassLoader();
        if (cl != null) {
            cls.add(cl);
        }
        cl = ClassLoader.getSystemClassLoader();
        if (cl != null) {
            cls.add(cl);
        }

        InputStream is = null;
        for (ClassLoader classLoader : cls) {
            is = classLoader.getResourceAsStream(file);
            if (is != null) {
                break;
            }
        }

        if (is != null) {
            return is;
        } else if (required) {
            throw new IllegalArgumentException("Can't locate config file " + file);
        }
        return null;
    }

    @Override
    public List<String> getAllServiceName() {
        return instanceMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public List<ServerInstance> getAllServerList() {
        return getServer(null);
    }

    @Override
    public List<ServerInstance> getServerList(String serviceName) {
        return getServer(serviceName);
    }

    private List<ServerInstance> getServer(String serviceName) {
        List<ServerInstance> instances = new LinkedList<>();
        if (StringUtils.isEmpty(serviceName)) {
            instanceMap.forEach((k, v) -> instances.addAll(v));
            return instances;
        }
        return instanceMap.get(serviceName);
    }

    @Override
    public void refreshServerList() {
        loadServerFromConfigFile();
    }

    @Override
    public void refreshServerList(String serviceName) {
        loadServerFromConfigFile();
    }

    @Override
    public List<ServerInstance> setServerList(List<ServerInstance> instanceList) {
        instanceList.forEach(ServerInstance::ready);
        this.instanceMap = StreamTools.groupBy(instanceList, ServerInstance::getServiceName);
        return instanceList;
    }

    @Override
    public List<ServerInstance> addServerList(List<ServerInstance> instanceList) {

        instanceList.forEach(ServerInstance::ready);
        Map<String, List<ServerInstance>> map = StreamTools.groupBy(instanceList, ServerInstance::getServiceName);


        this.instanceMap.forEach((k, v) -> {
            if (map.containsKey(k)) {
                v.addAll(map.get(k));
            }
        });

        return getAllServerList();
    }


    @Data
    public static class YamlServerConfig {
        private List<YamlServerList> servers;
    }

    @Data
    public static class YamlServerList {
        private List<ServerInstance> instances;
        private String serviceName;
    }


}
