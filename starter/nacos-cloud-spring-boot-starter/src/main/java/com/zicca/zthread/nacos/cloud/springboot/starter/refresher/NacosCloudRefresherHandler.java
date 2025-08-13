package com.zicca.zthread.nacos.cloud.springboot.starter.refresher;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorHolder;
import com.zicca.zthread.core.executor.ThreadPoolExecutorProperties;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.executor.support.RejectedPolicyTypeEnum;
import com.zicca.zthread.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.zicca.zthread.core.parser.ConfigParserHandler;
import com.zicca.zthread.core.toolkit.ThreadPoolExecutorBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static com.zicca.zthread.core.constant.Constants.CHANGE_DELIMITER;
import static com.zicca.zthread.core.constant.Constants.CHANGE_THREAD_POOL_TEXT;

/**
 * Nacos Cloud 版本刷新处理器
 *
 * @author zicca
 */
@Slf4j
@RequiredArgsConstructor
public class NacosCloudRefresherHandler implements ApplicationRunner {

    private final ConfigService configService;
    private final BootstrapConfigProperties properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
        // 根据 dataId 和 group 共同确定唯一的配置文件位置
        configService.addListener(
                nacosConfig.getDataId(),
                nacosConfig.getGroup(),
                new Listener() { // 向指定的 dataId 和 group 注册监听器，一旦 Nacos 端对应的配置发生变更，监听器就会被自动触发。
                    // 自定义一个但线程池来异步执行回调逻辑，避免阻塞Nacos客户端的主线程，同时也规避了并发带来的副作用
                    @Override
                    public Executor getExecutor() {
                        return ThreadPoolExecutorBuilder.builder()
                                .corePoolSize(1)
                                .maximumPoolSize(1)
                                .keepAliveTime(9999L)
                                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                                .threadFactory("cloud-nacos-refresher-thread_")
                                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                                .build();
                    }

                    // 当配置发生变更时，该方法会被回调
                    @SneakyThrows
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 如果 Nacos 配置文件变更，会触发该方法进行回调
                        refreshThreadPoolProperties(configInfo);
                    }
                }
        );

        log.info("Dynamic thread pool refresher, add nacos cloud listener success. data-id: {}, group: {}", nacosConfig.getDataId(), nacosConfig.getGroup());
    }

    private void refreshThreadPoolProperties(String configInfo) throws IOException {
        Map<Object, Object> configInfoMap = ConfigParserHandler.getInstance().parseConfig(configInfo, properties.getConfigFileType());
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);
        Binder binder = new Binder(source);

        BootstrapConfigProperties refresherProperties = binder.bind(BootstrapConfigProperties.PREFIX, Bindable.ofInstance(properties)).get();
        log.info("Latest updated configuration: \n{}", configInfo);
        log.info("Java configuration object binding: \n{}", JSON.toJSONString(refresherProperties));
        // ...... 检测线程池参数是否变更，如果已变更则进行更新

        // 检查是否有线程池配置
        if (CollUtil.isEmpty(refresherProperties.getExecutors())) {
            return;
        }
        // 判断线程池配置是否发生变化
        // 逐个遍历线程池配置
        for (ThreadPoolExecutorProperties remoteProperties : refresherProperties.getExecutors()) {
            boolean changed = hasThreadPoolConfigChanged(remoteProperties);
            if (!changed) {
                continue;
            }
            // 变更线程池配置
            reSetThreadPoolProperties(remoteProperties);
        }

    }

    /**
     * 重置线程池配置
     *
     * @param remoteProperties 远程线程池配置
     */
    private void reSetThreadPoolProperties(ThreadPoolExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        ThreadPoolExecutorHolder holder = ZThreadRegistry.getHolder(threadPoolId);
        if (holder == null) {
            log.warn("No thread pool found for thread pool id: {}", threadPoolId);
            return;
        }
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();

        // 线程数更新：先最大后核心
        Integer remoteCorePoolSize = remoteProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = remoteProperties.getMaximumPoolSize();
        if (remoteCorePoolSize != null && remoteMaximumPoolSize != null) {
            int originalMaximumPoolSize = executor.getMaximumPoolSize();
            // 如果远程核心线程数大于当前最大线程数，需要先调大当前最大线程数，否则抛异常 IllegalArgumentException
            /**
             * JDK17 线程池底层在设置核心线程数时做了参数限制校验
             * {@link ThreadPoolExecutor#setCorePoolSize(int)}
             */
            if (remoteCorePoolSize > originalMaximumPoolSize) {
                executor.setMaximumPoolSize(remoteMaximumPoolSize);
                executor.setCorePoolSize(remoteCorePoolSize);
            } else {
                executor.setCorePoolSize(remoteCorePoolSize);
                executor.setMaximumPoolSize(remoteMaximumPoolSize);
            }
        } else {
            if (remoteCorePoolSize != null) {
                executor.setMaximumPoolSize(remoteCorePoolSize);
            }
            if (remoteMaximumPoolSize != null) {
                executor.setCorePoolSize(remoteMaximumPoolSize);
            }
        }

        // 拒绝策略
        if (remoteProperties.getRejectedHandler() != null &&
                !Objects.equals(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler())) {
            RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(remoteProperties.getRejectedHandler());
            executor.setRejectedExecutionHandler(handler);
        }

        // 队列容量动态扩容
        if (isQueueCapacityChanged(originalProperties, remoteProperties, executor)) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            ResizableCapacityLinkedBlockingQueue<?> resizableQueue = (ResizableCapacityLinkedBlockingQueue<?>) queue;
            resizableQueue.setCapacity(remoteProperties.getQueueCapacity());
        }

        // 刷新元数据、发送通知、打印审计日志
        holder.setExecutorProperties(remoteProperties);

        // 发送线程池配置变更消息通知
//        sendThreadPoolConfigChangeMessage(originalProperties, remoteProperties);

        // 打印线程池配置变更日志
        log.info(CHANGE_THREAD_POOL_TEXT,
                threadPoolId,
                String.format(CHANGE_DELIMITER, originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()),
                String.format(CHANGE_DELIMITER, originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()),
                String.format(CHANGE_DELIMITER, originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()),
                String.format(CHANGE_DELIMITER, originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()),
                String.format(CHANGE_DELIMITER, originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()),
                String.format(CHANGE_DELIMITER, originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut()));
    }


    /**
     * 检查线程池配置是否发生变化
     *
     * @param remoteProperties 远程线程池配置
     * @return 线程池配置是否发生变化
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
     * 逐字段比较关键属性检查两个线程池配置是否发生变化
     *
     * @param original 本地线程池配置
     * @param remote   远程线程池配置
     * @param executor 线程池实例
     * @return 线程池配置是否发生变化
     */
    private boolean hasDifference(ThreadPoolExecutorProperties original,
                                  ThreadPoolExecutorProperties remote,
                                  ThreadPoolExecutor executor) {
        return isChanged(original.getCorePoolSize(), remote.getCorePoolSize())
                || isChanged(original.getMaximumPoolSize(), remote.getMaximumPoolSize())
                || isChanged(original.getAllowCoreThreadTimeOut(), remote.getAllowCoreThreadTimeOut())
                || isChanged(original.getKeepAliveTime(), remote.getKeepAliveTime())
                || isChanged(original.getRejectedHandler(), remote.getRejectedHandler())
                || isQueueCapacityChanged(original, remote, executor);
    }

    /**
     * 判断两个对象是否发生变化，忽略空值，仅对非 null 字段进行判断
     *
     * @param before 变化前
     * @param after  变化后
     * @param <T>    泛型
     * @return 是否发生变化
     */
    private <T> boolean isChanged(T before, T after) {
        return after != null && !Objects.equals(before, after);
    }

    /**
     * 判断队列容量是否发生变化，仅限自定义阻塞队列
     *
     * @param original 本地线程池配置
     * @param remote   远程线程池配置
     * @param executor 线程池实例
     * @return 队列容量是否发生变化
     */
    private boolean isQueueCapacityChanged(ThreadPoolExecutorProperties original,
                                           ThreadPoolExecutorProperties remote,
                                           ThreadPoolExecutor executor) {
        Integer remoteCapacity = remote.getQueueCapacity();
        Integer originalCapacity = original.getQueueCapacity();
        BlockingQueue<Runnable> queue = executor.getQueue();

        return remoteCapacity != null
                && !Objects.equals(originalCapacity, remoteCapacity)
                && Objects.equals("ResizableCapacityLinkedBlockingQueue", queue.getClass().getSimpleName());
    }

}
