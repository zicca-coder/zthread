package com.zicca.zthread.core.monitor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.zicca.zthread.core.config.ApplicationProperties;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorHolder;
import com.zicca.zthread.core.executor.ZThreadExecutor;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.monitor.support.AtomicDeltaWrapper;
import com.zicca.zthread.core.monitor.support.DeltaWrapper;
import com.zicca.zthread.core.monitor.support.SynchronizedDeltaWrapper;
import com.zicca.zthread.core.toolkit.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 线程池运行时监控器
 *
 * @author zicca
 */
@Slf4j
public class ThreadPoolMonitor {

    private ScheduledExecutorService scheduler;
    private Map<String, ThreadPoolRuntimeInfo> micrometerMonitorCache;
    private Map<String, DeltaWrapper> rejectCountDeltaMap;
    private Map<String, DeltaWrapper> completedTaskDeltaMap;


    private static final String METRIC_NAME_PREFIX = "dynamic.thread-pool";
    private static final String DYNAMIC_THREAD_POOL_ID_TAG = METRIC_NAME_PREFIX + ".id";
    private static final String APPLICATION_NAME_TAG = "application.name";

    /**
     * 启动定时检查任务
     */
    public void start() {
        BootstrapConfigProperties.MonitorConfig monitorConfig =
                BootstrapConfigProperties.getInstance().getMonitorConfig();

        if (!monitorConfig.getEnable()) {
            return;
        }

        // 初始化监控相关资源
        micrometerMonitorCache = new ConcurrentHashMap<>();
        rejectCountDeltaMap = new ConcurrentHashMap<>();
        completedTaskDeltaMap = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(
                1,
                ThreadFactoryBuilder.builder()
                        .namePrefix("scheduler_thread-pool_monitor")
                        .build()
        );


        // 每指定时间检查一次，初始延迟0s
        scheduler.scheduleWithFixedDelay(() -> {
            Collection<ThreadPoolExecutorHolder> holders = ZThreadRegistry.getAllHolders();
            for (ThreadPoolExecutorHolder holder : holders) {
                ThreadPoolRuntimeInfo runtimeInfo = buildThreadPoolRuntimeInfo(holder);

                // todo: 优化为策略模式，方便后续扩展监控类型
                if (Objects.equals(monitorConfig.getCollectType(), "log")) {
                    logMonitor(runtimeInfo);
                } else if (Objects.equals(monitorConfig.getCollectType(), "micrometer")) {
                    micrometerMonitor(runtimeInfo);
                }
            }
        }, 0, monitorConfig.getCollectInterval(), TimeUnit.SECONDS);
    }


    /**
     * 停止报警检查
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }


    /**
     * 本地日志输出
     *
     * @param runtimeInfo 运行时信息
     */
    private void logMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        log.info("[ThreadPool Monitor] {} | Content: {}",
                runtimeInfo.getThreadPoolId(),
                JSON.toJSON(runtimeInfo));
    }


    /**
     * 采集 Micrometer 监控信息
     * <p>
     *     静态指标：仅注册一次
     *     动态指标：持续更新
     *
     * @param runtimeInfo 运行时信息
     */
    private void micrometerMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        String threadPoolId = runtimeInfo.getThreadPoolId();
        ThreadPoolRuntimeInfo existingRuntimeInfo = micrometerMonitorCache.get(threadPoolId);

        // 只在首次注册时绑定 Gauge
        if (Objects.isNull(existingRuntimeInfo)) {
            Iterable<Tag> tags = CollectionUtil.newArrayList(
                    Tag.of(DYNAMIC_THREAD_POOL_ID_TAG, threadPoolId),
                    Tag.of(APPLICATION_NAME_TAG, ApplicationProperties.getApplicationName())
            );

            // todo:
            ThreadPoolRuntimeInfo registerRuntimeInfo = BeanUtil.toBean(runtimeInfo, ThreadPoolRuntimeInfo.class);
            micrometerMonitorCache.put(threadPoolId, registerRuntimeInfo);

            // 注册各种静态指标（如核心线程数、最大线程数等）
            Metrics.gauge(metricName("cpre.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCorePoolSize);
            Metrics.gauge(metricName("maximum.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getMaximumPoolSize);
            Metrics.gauge(metricName("current.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCurrentPoolSize);
            Metrics.gauge(metricName("largest.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getLargestPoolSize);
            Metrics.gauge(metricName("active.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getActivePoolSize);
            Metrics.gauge(metricName("queue.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueSize);
            Metrics.gauge(metricName("queue.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueCapacity);
            Metrics.gauge(metricName("queue.remaining.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueRemainingCapacity);

            // 注册增量 delta 指标（完成任务数、拒绝任务数等）
            DeltaWrapper completedDelta = new AtomicDeltaWrapper();
            completedTaskDeltaMap.put(threadPoolId, completedDelta);
            Metrics.gauge(metricName("completed.task.count"), tags, completedDelta, DeltaWrapper::getDelta);

            DeltaWrapper rejectDelta = new AtomicDeltaWrapper();
            rejectCountDeltaMap.put(threadPoolId, rejectDelta);
            Metrics.gauge(metricName("reject.count"), tags, rejectDelta, DeltaWrapper::getDelta);
        } else {
            // 更新属性（避免重复注册 Gauge）
            // todo:
            BeanUtil.copyProperties(runtimeInfo, existingRuntimeInfo);
        }

        // 每次都更新 delta 值
        completedTaskDeltaMap.get(threadPoolId).update(runtimeInfo.getCompletedTaskCount());
        rejectCountDeltaMap.get(threadPoolId).update(runtimeInfo.getRejectCount());

    }


    private String metricName(String name) {
        return String.join(".", METRIC_NAME_PREFIX, name);
    }


    /**
     * 构建运行时信息
     *
     * @param holder 线程池持有者
     * @return 运行时信息
     */
    @SneakyThrows
    private ThreadPoolRuntimeInfo buildThreadPoolRuntimeInfo(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BlockingQueue<?> queue = executor.getQueue();

        long rejectCount = -1L;
        if (executor instanceof ZThreadExecutor) {
            rejectCount = ((ZThreadExecutor) executor).getRejectCount().get();
        }

        int workQueueSize = queue.size();
        int remainingCapacity = queue.remainingCapacity();

        return ThreadPoolRuntimeInfo.builder()
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolSize(executor.getActiveCount())  // API 有锁，避免高频率调用
                .currentPoolSize(executor.getPoolSize())  // API 有锁，避免高频率调用
                .completedTaskCount(executor.getCompletedTaskCount())  // API 有锁，避免高频率调用
                .largestPoolSize(executor.getLargestPoolSize())  // API 有锁，避免高频率调用
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(workQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(workQueueSize + remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectCount(rejectCount)
                .build();
    }

}
