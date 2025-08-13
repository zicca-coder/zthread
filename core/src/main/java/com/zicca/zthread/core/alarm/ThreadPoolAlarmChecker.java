package com.zicca.zthread.core.alarm;

import cn.hutool.core.date.DateUtil;
import com.zicca.zthread.core.config.ApplicationProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorHolder;
import com.zicca.zthread.core.executor.ThreadPoolExecutorProperties;
import com.zicca.zthread.core.executor.ZThreadExecutor;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.zicca.zthread.core.notification.service.NotifierDispatcher;
import com.zicca.zthread.core.toolkit.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * 线程池运行状态报警检查器
 *
 * @author zicca
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolAlarmChecker {


    private final NotifierDispatcher notifierDispatcher;


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder.builder()
                    .namePrefix("scheduler_thread-pool_alarm_checker")
                    .build()
    );

    /**
     * 启动定时检查任务
     */
    public void start() {
        // 每5s检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(this::checkAlarm, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 停止报警检查
     */
    public void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 报警检查核心逻辑
     */
    private void checkAlarm() {
        try {
            Collection<ThreadPoolExecutorHolder> holders = ZThreadRegistry.getAllHolders();
            for (ThreadPoolExecutorHolder holder : holders) {
                if (holder.getExecutorProperties().getAlarm().getEnable()) {
                    checkActiveRate(holder);
                    checkQueueUsage(holder);
//                checkRejectCount(holder);
                }
            }
        } catch (Throwable throwable) {
            log.error("[ZThread] 线程池告警检查异常", throwable);
        }
    }


    /**
     * 检查线程活跃率（活跃线程数 / 最大线程数）
     */
    private void checkActiveRate(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        /**
         * API 有锁，避免高频调用
         * {@link ThreadPoolExecutor#getActiveCount()}
         */
        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();

        if (maximumPoolSize == 0) {
            return;
        }

        int activeRate = (int) Math.round((activeCount * 100.0) / maximumPoolSize);
        Integer threshold = properties.getAlarm().getActiveThreshold();

        if (activeRate >= threshold) {
            // 发送报警信息
            // .....
            sendAlarmMessage(AlarmTypeEnum.ACTIVITY.getValue(), holder);
        }
    }


    /**
     * 检查队列使用率（队列中任务数 / 队列容量）
     */
    private void checkQueueUsage(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();

        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueSize = queue.size();
        int capacity = queueSize + queue.remainingCapacity();

        if (capacity == 0) {
            return;
        }

        int usageRate = (int) Math.round((queueSize * 100.0) / capacity);
        Integer threshold = properties.getAlarm().getQueueThreshold();

        if (usageRate >= threshold) {
            // 触发报警
            // .....
            sendAlarmMessage(AlarmTypeEnum.CAPACITY.getValue(), holder);
        }

    }


    @SneakyThrows
    private void sendAlarmMessage(String alarmType, ThreadPoolExecutorHolder holder) {
        AlarmTypeEnum.of(alarmType);
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
        BlockingQueue<Runnable> queue = executor.getQueue();

        long rejectCount = -1L;
        if (executor instanceof ZThreadExecutor) {
            rejectCount = ((ZThreadExecutor) executor).getRejectCount().get();
        }

        int workQueueSize = queue.size(); // API 有锁，避免高频率调用
        int remainingCapacity = queue.remainingCapacity(); // API 有锁，避免高频率调用

        ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
                .applicationName(ApplicationProperties.getApplicationName())
                .activeProfile(ApplicationProperties.getActiveProfile())
                .identify(InetAddress.getLocalHost().getHostAddress())
                .alarmType(alarmType)
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
                .receives(properties.getNotify().getReceives())
                .currentTime(DateUtil.now())
                .interval(properties.getNotify().getInterval())
                .build();
        notifierDispatcher.sendAlarmMessage(alarm);
    }


}
