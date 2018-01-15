package com.github.df.restypass.lb;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.lb.server.ServerInstance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 负载均衡
 * 随机算法
 * copy from dubbo
 * Created by darrenfu on 17-6-28.
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "random";
    //TODO 伪随机问题
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    @Override
    protected ServerInstance doChoose(List<ServerInstance> instanceList, RestyCommand command) {


        int length = instanceList.size(); // 总个数
        int totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        for (int i = 0; i < length; i++) {
            int weight = getWeight(instanceList.get(i));
            totalWeight += weight; // 累计总权重
            if (sameWeight && i > 0
                    && weight != getWeight(instanceList.get(i - 1))) {
                sameWeight = false; // 计算所有权重是否一样
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                offset -= getWeight(instanceList.get(i));
                if (offset < 0) {
                    return instanceList.get(i);
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return instanceList.get(random.nextInt(length));
    }
}
