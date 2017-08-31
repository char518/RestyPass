package com.github.df.restypass.lb.server;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Spring cloud 使用consul作为注册中心
 * 可自动过滤consul中critical状态的instance
 * Created by darrenfu on 17-8-17.
 */
public class CloudConsulServerContext extends CloudDiscoveryServerContext {

    private static final Logger log = LoggerFactory.getLogger(CloudConsulServerContext.class);

    private ConsulClient consulClient;

    @Override
    protected void init(ApplicationContext applicationContext) {
        super.init(applicationContext);
        this.consulClient = applicationContext.getBean(ConsulClient.class);
    }

    @Override
    protected boolean isDiscoveryEnabled() {
        return consulClient != null;
    }

    @Override
    protected List<String> getServiceNames() {
        return super.getServiceNames();
    }

    @Override
    protected List<ServerInstance> getServiceInstances(String serviceName) {
        List<ServiceInstance> instances = new ArrayList<>();

        Response<List<HealthService>> healthServices = consulClient.getHealthServices(serviceName, true, QueryParams.DEFAULT);
        for (HealthService healthService : healthServices.getValue()) {
            if (isPassingChecks(healthService)) {
                String host = findHost(healthService);
                instances.add(new DefaultServiceInstance(serviceName, host,
                        healthService.getService().getPort(), false, getMetadata(healthService)));
            }
        }
        return convertToServerInstanceList(instances);
    }

    /**
     * Is passing checks boolean.
     *
     * @param service the service
     * @return the boolean
     */
    public boolean isPassingChecks(HealthService service) {
        for (Check check : service.getChecks()) {
            if (check.getStatus() != Check.CheckStatus.PASSING) {
                return false;
            }
        }
        return true;
    }

    private static String findHost(HealthService healthService) {
        HealthService.Service service = healthService.getService();
        HealthService.Node node = healthService.getNode();

        if (org.springframework.util.StringUtils.hasText(service.getAddress())) {
            return service.getAddress();
        } else if (org.springframework.util.StringUtils.hasText(node.getAddress())) {
            return node.getAddress();
        }
        return node.getNode();
    }

    private static Map<String, String> getMetadata(HealthService healthService) {
        return getMetadata(healthService.getService().getTags());
    }

    private static Map<String, String> getMetadata(List<String> tags) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        if (tags != null) {
            for (String tag : tags) {
                String[] parts = org.springframework.util.StringUtils.delimitedListToStringArray(tag, "=");
                switch (parts.length) {
                    case 0:
                        break;
                    case 1:
                        metadata.put(parts[0], parts[0]);
                        break;
                    case 2:
                        metadata.put(parts[0], parts[1]);
                        break;
                    default:
                        String[] end = Arrays.copyOfRange(parts, 1, parts.length);
                        metadata.put(parts[0], org.springframework.util.StringUtils.arrayToDelimitedString(end, "="));
                        break;
                }

            }
        }

        return metadata;
    }
}
