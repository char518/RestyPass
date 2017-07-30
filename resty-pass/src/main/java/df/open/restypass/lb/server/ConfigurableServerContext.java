package df.open.restypass.lb.server;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 配置化
 * 服务容器
 * Created by darrenfu on 17-6-28.
 */
public class ConfigurableServerContext implements ServerContext {

    {
        InputStream inputStream = parseYamlFile("resty-server.properties", true);
        Yaml yaml = new Yaml();
        yaml.loadAs(inputStream, ServerInstance.class);
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

    private List<ServerInstance> getServer(String serviceName) {
        List<ServerInstance> list = new ArrayList<>();
        list.add(ServerInstance.buildInstance(serviceName, "localhost", 9201));
        list.add(ServerInstance.buildInstance(serviceName, "localhost", 9202));
        return list;
    }


}
