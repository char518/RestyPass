package com.github.df.restypass.lb;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.lb.rule.VersionRule;
import com.github.df.restypass.lb.server.ServerContext;
import com.github.df.restypass.lb.server.ServerInstance;
import com.github.df.restypass.util.CommonTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.github.df.restypass.base.RestyConst.Instance.*;

/**
 * 负载均衡器，顶级抽象类
 * Created by darrenfu on 17-7-31.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {
    private static Logger log = LoggerFactory.getLogger(AbstractLoadBalancer.class);

    @Override
    public ServerInstance choose(ServerContext context, RestyCommand command, Set<String> excludeInstanceIdSet) {

        if (context == null || command == null || StringUtils.isEmpty(command.getServiceName())) {
            return null;
        }
        List<ServerInstance> serverList = context.getServerList(command.getServiceName());

        if (serverList == null || serverList.size() == 0) {
            return null;
        }
        List<ServerInstance> usableServerList = null;
        final int instanceSize = serverList.size();

        //移除未初始化完成或当前状态未存活的实例
        Iterator<ServerInstance> iterator = serverList.iterator();
        while (iterator.hasNext()) {
            ServerInstance instance = iterator.next();
            if (!instance.isReady()
                    || !instance.getIsAlive()
                    || !isVersionOk(command, instance)) {
                iterator.remove();
                if (log.isTraceEnabled()) {
                    log.trace("剔除本次不可使用的server实例:{}", instance);
                }
            } else if (shouldExcludeInstance(excludeInstanceIdSet, instance)) {
                //剔除被排除的实例ID
                if (usableServerList == null) {
                    usableServerList = new ArrayList<>(instanceSize);
                }
                iterator.remove();
                if (log.isTraceEnabled()) {
                    log.trace("剔除本次被排除的server实例:{}", instance);
                }
                usableServerList.add(instance);
            }
        }

        if (CommonTools.isEmpty(serverList) && CommonTools.isEmpty(usableServerList)) {
            return null;
        }
        // 排除excludeServer后，有可用server则使用，否则还是使用原始的Server
        // 避免失败重试导致无instance可用
        if (CommonTools.isEmpty(serverList) && CommonTools.isNotEmpty(usableServerList)) {
            serverList = usableServerList;
        }

        //只有一个实例，直接返回，忽视排除的实例ID
        if (serverList.size() == 1) {
            return serverList.get(0);
        }
        return doChoose(serverList, command);
    }

    /**
     * 验证版本是否OK
     *
     * @param command  the command
     * @param instance the instance
     * @return boolean boolean
     */
    protected boolean isVersionOk(RestyCommand command, ServerInstance instance) {
        List<VersionRule> versionRules = command.getRestyCommandConfig().getVersion();
        if (versionRules == null || versionRules.size() == 0) {
            return true;
        }

        for (VersionRule versionRule : versionRules) {
            //版本不匹配版本规则
            if (!versionRule.match(instance.getVersion())) {
                return false;
            }
        }
        // 匹配成功
        return true;
    }

    /**
     * 判断instance是否已被排除
     *
     * @param excludeInstanceIdSet
     * @param instance
     * @return
     */
    private boolean shouldExcludeInstance(Set<String> excludeInstanceIdSet, ServerInstance instance) {
        return CommonTools.isNotEmpty(excludeInstanceIdSet) && excludeInstanceIdSet.contains(instance.getInstanceId());
    }

    /**
     * 基于特定算法，选举可用server实例
     *
     * @param instanceList the instance list
     * @param command      the command
     * @return the server instance
     */
    protected abstract ServerInstance doChoose(List<ServerInstance> instanceList, RestyCommand command);

    /**
     * 计算权重
     * copy from dubbo
     *
     * @param serverInstance the server instance
     * @return weight weight
     */
    int getWeight(ServerInstance serverInstance) {
        int weight = serverInstance.getWeight();

        if (weight > 0) {
            long timestamp = serverInstance.getStartTimestamp();
            if (timestamp > 0L) {
                int uptime = (int) (System.currentTimeMillis() - timestamp);
                int warmup = serverInstance.getWarmupSeconds() * 1000;

                if (uptime > 0 && uptime < warmup) {
                    weight = calculateWarmupWeight(uptime, warmup, weight);
                }
            }
        }

        return weight;
    }

    /**
     * 根据启动时间和预热时间计算权重
     *
     * @param uptime 已经启动的时间 ms
     * @param warmup 预热时间 ms
     * @param weight 权重
     * @return the int
     */
    private int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }

}
