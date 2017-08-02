package df.open.restypass.lb;

import df.open.restypass.command.RestyCommand;
import df.open.restypass.lb.server.ServerContext;
import df.open.restypass.lb.server.ServerInstance;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

import static df.open.restypass.base.RestyConst.Instance.*;

/**
 * 负载均衡器，顶级抽象类
 * Created by darrenfu on 17-7-31.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public ServerInstance choose(ServerContext context, RestyCommand command, Set<String> excludeInstanceIdSet) {

        if (context == null || command == null || StringUtils.isEmpty(command.getServiceName())) {
            return null;
        }
        List<ServerInstance> serverList = context.getServerList(command.getServiceName());

        if (serverList == null || serverList.size() == 0) {
            return null;
        }
        if (serverList.size() == 1) {
            if (excludeInstanceIdSet != null
                    && excludeInstanceIdSet.size() > 0
                    && excludeInstanceIdSet.contains(serverList.get(0).getInstanceId())) {
                return null;
            }
            return serverList.get(0);
        }

        if (excludeInstanceIdSet != null && excludeInstanceIdSet.size() > 0) {
            serverList.removeIf(v -> excludeInstanceIdSet.contains(v.getInstanceId()));
        }

        return doChoose(serverList, command);
    }


    /**
     * 基于特定算法，选举可用server实例
     *
     * @param instanceList         the instance list
     * @param command              the command
     * @return the server instance
     */
    protected abstract ServerInstance doChoose(List<ServerInstance> instanceList, RestyCommand command);

    /**
     * copy from dubbo
     *
     * @param serverInstance the server instance
     * @return weight
     */
    protected int getWeight(ServerInstance serverInstance) {
        int weight = serverInstance.getPropValue(PROP_WEIGHT_KEY, PROP_WEIGHT_DEFAULT);

        if (weight > 0) {
            long timestamp = serverInstance.getPropValue(PROP_TIMESTAMP_KEY, PROP_TIMESTAMP_DEFAULT);
            if (timestamp > 0L) {
                int uptime = (int) (System.currentTimeMillis() - timestamp);
                int warmup = serverInstance.getPropValue(PROP_WARMUP_KEY, PROP_WARMUP_DEFAULT);

                if (uptime > 0 && uptime < warmup) {
                    weight = caculateWarmupWeight(uptime, warmup, weight);
                }
            }
        }

        return weight;
    }

    /**
     * Caculate warmup weight int.
     *
     * @param uptime the uptime
     * @param warmup the warmup
     * @param weight the weight
     * @return the int
     */
    int caculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }

}
