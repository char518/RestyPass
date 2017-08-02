package df.open.restypass.lb.server;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置化
 * 服务容器
 * Created by darrenfu on 17-6-28.
 */
public class ConfigurableServerContext implements ServerContext {

    private ConcurrentHashMap<String, List<ServerInstance>> instanceMap;


    public ConfigurableServerContext() {
        this.instanceMap = new ConcurrentHashMap<>();
        List<YamlServerList> servers = loadServerFromConfigFile();
        for (YamlServerList server : servers) {
            for (ServerInstance instance : server.getInstances()) {
                instance.setServiceName(server.getServiceName());
                instance.init();
            }
            instanceMap.put(server.getServiceName(), server.getInstances());
        }
    }

    private List<YamlServerList> loadServerFromConfigFile() {
        InputStream inputStream = parseYamlFile("resty-server.yaml", true);
        Yaml yaml = new Yaml();
        YamlServerConfig config = yaml.loadAs(inputStream, YamlServerConfig.class);
        return config.servers;
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
        return null;
    }

    @Override
    public List<ServerInstance> getAllServerList() {
        return getServer(null);
    }

    @Override
    public List<ServerInstance> getServerList(String serviceName) {
        return getServer(serviceName);
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

    private List<ServerInstance> getServer(String serviceName) {

        return instanceMap.get(serviceName);
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
