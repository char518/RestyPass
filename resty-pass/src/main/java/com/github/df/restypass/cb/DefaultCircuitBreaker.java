package com.github.df.restypass.cb;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.enums.CircuitBreakerStatus;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.RequestException;
import com.github.df.restypass.lb.server.ServerInstance;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认断路器
 * Created by darrenfu on 17-7-23.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@ToString(exclude = {"commandQueue", "halfOpenLock"})
public class DefaultCircuitBreaker implements CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(DefaultCircuitBreaker.class);

    /**
     * key:[RestyCommand#path, ServerInstance#instanceId, metricsKey]
     */
    private static Table<String, String, String> keyTable = HashBasedTable.create();

    // TODO 熔断条件配置化
    /**
     * 短路条件：失败请求占总请求的比例
     */
    private static Integer breakFailPercentage = 50;

    /**
     * 短路条件：失败请求最低数量
     */
    private static Integer breakFailCount = 10;
    /**
     * 短路条件：连续失败次数
     */
    private static Integer continuousFailCount = 10;

    /**
     * 断路器半开的时间间隔，Half_Open时，断路器会放行一个请求，如果成功->Open，如果失败->Break
     */
    private static Long halfOpenMilliseconds = 1000 * 10L;

    /**
     * Event key的前缀
     */
    private static final String KEY_PREFIX = "default-breaker-";

    /**
     * 注册事件使用Key
     */
    private String eventKey;

    /**
     * 半开状态使用的锁，保证只有一个请求通过
     */
    private ReentrantLock halfOpenLock;

    private ConcurrentHashMap<String, ReentrantLock> halfOpenLockMap;

    /**
     * 启动锁
     */
    private ReentrantLock startLock;

    /**
     * 统计结果缓冲区 metricsKey->Metrics
     */
    private ConcurrentHashMap<String, Metrics> segmentMap;

    /**
     * 记录短路状态 metricsKey->BreakerStatus
     */
    private ConcurrentHashMap<String, CircuitBreakerStatus> statusMap;

    /**
     * Command 阻塞队列
     */
    private LinkedBlockingQueue<RestyCommand> commandQueue;

    /**
     * 损坏的server的InstanceId列表
     */
    private Set<String> brokenServerSet;

    /**
     * 断路器是否启动
     */
    private boolean started;

    /**
     * Init Method
     */
    public DefaultCircuitBreaker() {
        this.started = false;
        this.startLock = new ReentrantLock();
    }

    @Override
    public String getEventKey() {
        return this.eventKey;
    }


    @Override
    public void start() {
        if (!started) {
            startLock.lock();
            try {
                if (!started) {
                    this.eventKey = KEY_PREFIX + UUID.randomUUID().toString().replace("-", "").toLowerCase();
                    this.segmentMap = new ConcurrentHashMap<>();
                    this.statusMap = new ConcurrentHashMap<>();
                    this.brokenServerSet = new CopyOnWriteArraySet<>();
                    this.commandQueue = new LinkedBlockingQueue<>();
                    this.halfOpenLock = new ReentrantLock();
                    this.halfOpenLockMap = new ConcurrentHashMap<>(64);
                    this.registerEvent();
                    this.startTask();
                    started = true;
                }
            } finally {
                startLock.unlock();
            }
        }
    }

    @Override
    public void end() {
        started = false;
    }

    @Override
    public boolean shouldPass(RestyCommand restyCommand, ServerInstance serverInstance) {
        // 未启动
        if (!started) {
            return true;
        }
        String metricsKey = getMetricsKey(restyCommand.getPath(), serverInstance.getInstanceId());

        // 强制短路
        CircuitBreakerStatus cbStatus = statusMap.get(metricsKey);

        if (restyCommand.getRestyCommandConfig().isForceBreakEnabled()) {
            statusMap.put(metricsKey, CircuitBreakerStatus.FORCE_BREAK);
            return false;
        } else if (CircuitBreakerStatus.FORCE_BREAK == cbStatus) {
            // 强制短路关闭后，OPEN
            statusMap.put(metricsKey, CircuitBreakerStatus.OPEN);
        }

        // 断路器未启用
        if (!restyCommand.getRestyCommandConfig().isCircuitBreakEnabled()) {
            return true;
        }

        // 获取统计段
        Metrics metrics = getCommandMetrics(metricsKey);
        if (metrics == null) {
            return true;
        }

        // 半开状态说明已经有请求通过断路器，尝试恢复短路，所以其它请求直接拒绝，继续熔断
        if (CircuitBreakerStatus.HALF_OPEN == cbStatus) {
            return false;
        }
        // 是否应该通过
        boolean shouldPass = true;
        // 获取计数器
        Metrics.SegmentMetrics segmentMetrics = metrics.getMetrics();
        // 计数器中失败数量和比例超过阀值，则触发短路判断
        if (segmentMetrics.getContinuousFailCount() >= continuousFailCount // 连续失败次数达到阀值
                // 失败比例以及失败数量达到阀值
                || (segmentMetrics.failCount() >= breakFailCount && segmentMetrics.failPercentage() >= breakFailPercentage)) {
            CircuitBreakerStatus breakerStatus = cbStatus;
            shouldPass = false;
            if (breakerStatus == null || breakerStatus == CircuitBreakerStatus.OPEN) {
                // 短路
                statusMap.put(metricsKey, CircuitBreakerStatus.BREAK);
            } else if (breakerStatus == CircuitBreakerStatus.HALF_OPEN) {
                // noop
            } else {
                // 如果上次的请求距离现在超过阀值，则允许一次试探请求
                long now = System.currentTimeMillis();
                if (segmentMetrics.last() != null && (now - segmentMetrics.last() > halfOpenMilliseconds)) {


                    final ReentrantLock lock = getHalfOpenLockInMap(metricsKey);
                    lock.lock();
                    try {
                        // 判断当前短路状态 确保只有一个请求通过
                        if (statusMap.get(metricsKey) == CircuitBreakerStatus.BREAK) {
                            statusMap.put(metricsKey, CircuitBreakerStatus.HALF_OPEN);
                            shouldPass = true;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }

            if (log.isDebugEnabled()) {
                if (shouldPass) {
                    log.debug("尝试恢复短路服务:{}:{},metrics:{}", restyCommand.getServiceName(), restyCommand.getPath(), metrics);
                } else {
                    log.debug("熔断服务:{}:{},metrics:{}", restyCommand.getServiceName(), restyCommand.getPath(), metrics);
                }
            }

        }
        return shouldPass;
    }

    /**
     * 获取半开锁
     *
     * @param metricsKey
     * @return
     */
    private ReentrantLock getHalfOpenLockInMap(String metricsKey) {
        ReentrantLock reentrantLock = halfOpenLockMap.get(metricsKey);
        if (reentrantLock == null) {
            halfOpenLockMap.putIfAbsent(metricsKey, new ReentrantLock());
            reentrantLock = halfOpenLockMap.get(metricsKey);
        }
        return reentrantLock;
    }

    @Override
    public Set<String> getBrokenServer() {
        // TODO 计算broken server
        return Collections.EMPTY_SET;
    }

    @Override
    public void setStatus(RestyCommand restyCommand, CircuitBreakerStatus status) {
        //TODO: impl it
        throw new UnsupportedOperationException("暂不支持此操作");
    }

    /**
     * 注册事件以及消费函数
     */
    private void registerEvent() {
        this.on(this.eventKey, (command) -> {
            if (command instanceof RestyCommand) {
                if (isQueueAvailable()) {
                    boolean offer = commandQueue.offer((RestyCommand) command);
                    if (!offer) {
                        throw new RuntimeException("Failed to add command into queue");
                    }
                }
            }
        });
    }

    /**
     * command阻塞队列是否可用
     *
     * @return 可用 true，不可用 false
     */
    private boolean isQueueAvailable() {
        return commandQueue != null && commandQueue.size() < 20000;
    }


    /**
     * 消费队列 统计已完成的command信息，更新 segment
     */
    private void startTask() {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("启动RestyCommand统计线程:" + this.eventKey);
            while (true) {
                try {
                    // 阻塞
                    RestyCommand firstCommand = commandQueue.take();
                    // 取出queue中所有的数据
                    List<RestyCommand> commandList = new LinkedList<>();
                    commandList.add(firstCommand);
                    commandQueue.drainTo(commandList);

                    for (RestyCommand restyCommand : commandList) {
                        String key = getMetricsKey(restyCommand.getPath(), restyCommand.getInstanceId());
                        // 获取 计数器
                        Metrics metrics = getCommandMetrics(key);
                        if (metrics == null) {
                            log.warn("获取计数器失败:{}", key);
                            continue;
                        }
                        boolean isSuccess = isCommandSuccessExecuted(restyCommand);
                        boolean forceUseNewMetrics = false;
                        // 如果当前处在短路或半短路状态
                        CircuitBreakerStatus breakerStatus = statusMap.get(key);
                        if ((breakerStatus == CircuitBreakerStatus.BREAK || breakerStatus == CircuitBreakerStatus.HALF_OPEN)) {
                            // 结果成功 则不再短路，打开断路器
                            if (isSuccess) {
                                // 并使用一个新的计数器
                                forceUseNewMetrics = true;
                                statusMap.put(key, CircuitBreakerStatus.OPEN);
                            } else {
                                // 否则恢复到短路状态
                                statusMap.put(key, CircuitBreakerStatus.BREAK);
                            }
                        }
                        metrics.store(isSuccess, forceUseNewMetrics);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("处理完成, 处理个数:{}，剩余:{}个", commandList.size(), commandQueue.size());
                    }
                } catch (Exception ex) {
                    log.error("断路器RestyCommand处理失败:{}", ex);
                }

            }
        });
    }


    /**
     * command是否成功执行
     *
     * @param restyCommand Resty请求
     * @return 请求是否成功
     */
    private boolean isCommandSuccessExecuted(RestyCommand restyCommand) {
        if (restyCommand.getStatus() == RestyCommandStatus.FAILED) {
            // 请求错误是客户端错误，此处认为请求已被成功执行
            return restyCommand.getFailException() instanceof RequestException;
        } else if (restyCommand.getStatus() == RestyCommandStatus.SUCCESS) {
            return true;
        }
        return false;
    }


    /**
     * 返回MetricsKey
     *
     * @param commandPath 请求 path
     * @param instanceId  server实例ID
     * @return key
     */
    private String getMetricsKey(String commandPath, String instanceId) {
        String metricsKey = keyTable.get(commandPath, instanceId);
        if (StringUtils.isEmpty(metricsKey)) {
            metricsKey = commandPath + instanceId;
            keyTable.put(commandPath, instanceId, metricsKey);
        }
        return metricsKey;
    }


    /**
     * 获取 segmentDeque
     *
     * @param metricsKey key
     * @return 计数器
     */
    private Metrics getCommandMetrics(String metricsKey) {
        if (segmentMap == null) {
            return null;
        }
        Metrics metrics = segmentMap.get(metricsKey);
        if (metrics == null) {
            Metrics newMetrics = new Metrics();
            // putIfAbsent
            segmentMap.putIfAbsent(metricsKey, newMetrics);
            metrics = segmentMap.get(metricsKey);
        }
        return metrics;

    }


}
