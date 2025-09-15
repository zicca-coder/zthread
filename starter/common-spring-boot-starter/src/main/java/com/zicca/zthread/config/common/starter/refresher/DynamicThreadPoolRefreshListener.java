package com.zicca.zthread.config.common.starter.refresher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorHolder;
import com.zicca.zthread.core.executor.ThreadPoolExecutorProperties;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.executor.support.RejectedPolicyTypeEnum;
import com.zicca.zthread.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.zicca.zthread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.zicca.zthread.core.notification.service.NotifierDispatcher;
import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zicca.zthread.core.constant.Constants.CHANGE_DELIMITER;
import static com.zicca.zthread.core.constant.Constants.CHANGE_THREAD_POOL_TEXT;

/**
 * 动态线程池监听配置中心刷新事件
 *
 * @author zicca
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {

    private final NotifierDispatcher notifierDispatcher;

    /**
     * 处理线程池配置更新事件
     * <p>
     * 当配置中心的线程池配置发生变更时，该方法会被触发执行。主要流程如下：
     * 1. 获取变更后的线程池配置信息
     * 2. 遍历所有线程池配置，逐个检查是否发生变化
     * 3. 对发生变化的线程池进行配置更新
     * 4. 发送配置变更通知消息
     * 5. 记录配置变更日志
     *
     * @param event 线程池配置更新事件，包含最新的配置信息
     */
    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {
        // 获取配置中心最新的线程池配置
        BootstrapConfigProperties refresherProperties = event.getBootstrapConfigProperties();

        // 如果没有配置线程池，则直接返回
        if (CollUtil.isEmpty(refresherProperties.getExecutors())) {
            return;
        }

        // 遍历所有线程池配置
        for (ThreadPoolExecutorProperties remoteProperties : refresherProperties.getExecutors()) {
            String threadPoolId = remoteProperties.getThreadPoolId();

            // 以线程池 ID 为粒度加锁，避免多个线程同时刷新同一个线程池
            synchronized (threadPoolId.intern()) {
                // 检查线程池配置是否发生变化
                boolean changed = hasThreadPoolConfigChanged(remoteProperties);
                if (!changed) {
                    continue;
                }

                // 根据远程配置更新线程池参数
                updateThreadPoolFromRemoteConfig(remoteProperties);

                // 获取线程池持有者和原始配置
                ThreadPoolExecutorHolder holder = ZThreadRegistry.getHolder(threadPoolId);
                ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();

                // 更新线程池持有者的配置信息
                holder.setExecutorProperties(remoteProperties);

                // 打印线程池配置变更日志
                log.info(CHANGE_THREAD_POOL_TEXT,
                        threadPoolId,
                        String.format(CHANGE_DELIMITER, originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()),
                        String.format(CHANGE_DELIMITER, originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()),
                        String.format(CHANGE_DELIMITER, originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()),
                        String.format(CHANGE_DELIMITER, originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()),
                        String.format(CHANGE_DELIMITER, originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()),
                        String.format(CHANGE_DELIMITER, originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut())
                );

                // 发送线程池配置变更消息通知
                if (event.getBootstrapConfigProperties().getNotifyPlatforms().getEnable()) {
                    sendThreadPoolConfigChangeMessage(originalProperties, remoteProperties);
                }
            }
        }
    }


    /**
     * 检查线程池配置是否发生变化
     *
     * @param remoteProperties 远程配置属性
     * @return 配置是否发生变化
     */
    private boolean hasThreadPoolConfigChanged(ThreadPoolExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        ThreadPoolExecutorHolder holder = ZThreadRegistry.getHolder(threadPoolId);
        if (holder == null) {
            log.warn("No thread pool found for thread pool id: {}", threadPoolId);
            return false;
        }
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();
        return hasDifference(originalProperties, remoteProperties, executor);
    }

    /**
     * 检查线程池配置是否有变更
     *
     * @param originalProperties 本地线程池配置属性
     * @param remoteProperties   远程线程池配置属性
     * @param executor           线程池执行器
     * @return 是否有配置变更
     */
    private boolean hasDifference(ThreadPoolExecutorProperties originalProperties,
                                  ThreadPoolExecutorProperties remoteProperties,
                                  ThreadPoolExecutor executor) {
        return isChanged(originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize())
                || isChanged(originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize())
                || isChanged(originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut())
                || isChanged(originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime())
                || isChanged(originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler())
                || isQueueCapacityChanged(originalProperties, remoteProperties, executor);
    }

    /**
     * 检查配置项是否发生变化
     *
     * @param before 变更前的配置值
     * @param after  变更后的配置值
     * @param <T>    配置项类型
     * @return 配置项是否发生变化
     */
    private <T> boolean isChanged(T before, T after) {
        return after != null && !Objects.equals(before, after);
    }

    /**
     * 检查线程池队列容量是否发生变化
     *
     * @param originalProperties 本地线程池配置属性
     * @param remoteProperties   远程线程池配置属性
     * @param executor           线程池执行器
     * @return 队列容量是否发生变化
     */
    private boolean isQueueCapacityChanged(ThreadPoolExecutorProperties originalProperties, ThreadPoolExecutorProperties remoteProperties, ThreadPoolExecutor executor) {
        Integer remoteCapacity = remoteProperties.getQueueCapacity();
        Integer originalCapacity = originalProperties.getQueueCapacity();
        BlockingQueue<?> queue = executor.getQueue();

        return remoteCapacity != null
                && !Objects.equals(remoteCapacity, originalCapacity)
                && Objects.equals(BlockingQueueTypeEnum.RESIZE_CAPACITY_LINKED_BLOCKING_QUEUE.getName(), queue.getClass().getSimpleName());
    }


    /**
     * 根据远程配置更新线程池参数
     *
     * @param remoteProperties 远程配置属性
     */
    private void updateThreadPoolFromRemoteConfig(ThreadPoolExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        ThreadPoolExecutorHolder holder = ZThreadRegistry.getHolder(threadPoolId);
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();

        // 更新核心线程数和最大线程数
        Integer remoteCorePoolSize = remoteProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = remoteProperties.getMaximumPoolSize();
        if (remoteCorePoolSize != null && remoteMaximumPoolSize != null) {
            int originalMaximumPoolSize = executor.getMaximumPoolSize();
            // 如果新的核心线程数大于原来的最大线程数，需要先设置最大线程数再设置核心线程数
            if (remoteCorePoolSize > originalMaximumPoolSize) {
                executor.setMaximumPoolSize(remoteMaximumPoolSize);
                executor.setCorePoolSize(remoteCorePoolSize);
            } else {
                executor.setCorePoolSize(remoteCorePoolSize);
                executor.setMaximumPoolSize(remoteMaximumPoolSize);
            }
        } else {
            // 单独更新最大线程数或核心线程数
            if (remoteMaximumPoolSize != null) {
                executor.setMaximumPoolSize(remoteMaximumPoolSize);
            }
            if (remoteCorePoolSize != null) {
                executor.setCorePoolSize(remoteCorePoolSize);
            }
        }

        // 更新是否允许核心线程超时
        if (remoteProperties.getAllowCoreThreadTimeOut() != null &&
                !Objects.equals(remoteProperties.getAllowCoreThreadTimeOut(), originalProperties.getAllowCoreThreadTimeOut())) {
            executor.allowCoreThreadTimeOut(remoteProperties.getAllowCoreThreadTimeOut());
        }

        // 更新拒绝策略
        if (remoteProperties.getRejectedHandler() != null &&
                !Objects.equals(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler())) {
            RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(remoteProperties.getRejectedHandler());
            executor.setRejectedExecutionHandler(handler);
        }

        // 更新线程空闲存活时间
        if (remoteProperties.getKeepAliveTime() != null &&
                !Objects.equals(remoteProperties.getKeepAliveTime(), originalProperties.getKeepAliveTime())) {
            executor.setKeepAliveTime(remoteProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        }

        // 更新队列容量（仅对 ResizableCapacityLinkedBlockingQueue 生效）
        if (isQueueCapacityChanged(originalProperties, remoteProperties, executor)) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            ResizableCapacityLinkedBlockingQueue<?> resizableQueue = (ResizableCapacityLinkedBlockingQueue<?>) queue;
            resizableQueue.setCapacity(remoteProperties.getQueueCapacity());
        }
    }


    /**
     * 发送线程池配置变更通知消息
     *
     * @param originalProperties 原始线程池配置属性
     * @param remoteProperties  远程更新后的线程池配置属性
     */
    @SneakyThrows
    private void sendThreadPoolConfigChangeMessage(ThreadPoolExecutorProperties originalProperties,
                                                   ThreadPoolExecutorProperties remoteProperties) {
        Environment environment = ApplicationContextHolder.getBean(Environment.class);
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        String applicationName = environment.getProperty("spring.application.name");

        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = new HashMap<>();
        changes.put("corePoolSize", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()));
        changes.put("maximumPoolSize", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()));
        changes.put("queueCapacity", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()));
        changes.put("rejectedHandler", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()));
        changes.put("keepAliveTime", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()));

        ThreadPoolConfigChangeDTO configChangeDTO = ThreadPoolConfigChangeDTO.builder()
                .activeProfile(activeProfile)
                .identify(InetAddress.getLocalHost().getHostAddress())
                .applicationName(applicationName)
                .threadPoolId(originalProperties.getThreadPoolId())
                .workQueue(originalProperties.getWorkQueue())
                .receives(remoteProperties.getNotify().getReceives())
                .changes(changes)
                .updateTime(DateUtil.now())
                .build();
        notifierDispatcher.sendChangeMessage(configChangeDTO);
    }

}
