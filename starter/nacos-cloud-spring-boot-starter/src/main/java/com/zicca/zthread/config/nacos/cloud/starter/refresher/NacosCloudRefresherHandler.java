package com.zicca.zthread.config.nacos.cloud.starter.refresher;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.zicca.zthread.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.toolkit.ThreadPoolExecutorBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Nacos Cloud 版本刷新处理器
 *
 * @author zicca
 */
@Slf4j
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {

    private final ConfigService configService;
    private ThreadPoolExecutor refreshExecutor;

    public NacosCloudRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        super(properties);
        this.configService = configService;
    }

    public void start() {
        log.info("[RefresherHandler] Initializing refresher executor.");
        // 为监听器回调创建专用的单线程执行器
        refreshExecutor = ThreadPoolExecutorBuilder.builder()
                .corePoolSize(1)                    // 核心线程数为1
                .maximumPoolSize(1)                 // 最大线程数为1
                .keepAliveTime(9999L)              // 线程空闲存活时间
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)  // 使用同步队列
                .threadFactory("cloud-nacos-refresher-thread_")          // 线程名称前缀
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy()) // 拒绝策略：调用者运行
                .build();
    }


    /**
     * 注册 Nacos 配置监听器，用于监听线程池配置的变化
     * <p>
     * 该方法会向 Nacos 配置中心注册一个监听器，当指定的配置发生变化时，
     * 会通过独立的线程池异步触发配置刷新逻辑。
     *
     * @throws Exception 如果注册监听器过程中发生异常
     */
    @Override
    protected void registerListener() throws Exception {
        // 获取 Nacos 配置信息
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();

        // 添加配置监听器
        configService.addListener(
                nacosConfig.getDataId(),           // 配置的 dataId
                nacosConfig.getGroup(),            // 配置的 group
                new Listener() {
                    @Override
                    public Executor getExecutor() {
                        // 为监听器回调创建专用的单线程执行器
                        return refreshExecutor;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 当配置发生变化时，触发线程池属性刷新
                        refreshThreadPoolProperties(configInfo);
                    }
                }
        );

        // 记录监听器注册成功的日志
        log.info("[RefresherHandler] Add nacos config listener success, dataId={}, group={}", nacosConfig.getDataId(), nacosConfig.getGroup());
    }

    public void stop() {
        // 移除 Nacos 配置监听器
        try {
            BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
            if (configService != null && nacosConfig != null) {
                configService.removeListener(nacosConfig.getDataId(), nacosConfig.getGroup(), new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return refreshExecutor;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 空实现，仅用于移除监听器
                    }
                });
                log.info("[RefresherHandler] Removed nacos config listener success, dataId={}, group={}", nacosConfig.getDataId(), nacosConfig.getGroup());
            }
        } catch (Exception e) {
            log.error("[RefresherHandler] Failed to remove nacos config listener", e);
        }

        if (refreshExecutor != null && !refreshExecutor.isShutdown()) {
            log.info("[RefresherHandler] Start shutting down refresher executor.");
            refreshExecutor.shutdown();

            try {
                // 等待线程池正常终止，给予30秒时间完成正在进行的任务
                if (!refreshExecutor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                    log.warn("[RefresherHandler] Refresher executor did not terminate in 30 seconds, forcing shutdown");
                    refreshExecutor.shutdownNow();

                    // 再给10秒时间让任务强制关闭
                    if (!refreshExecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                        log.error("[RefresherHandler] Refresher executor could not be terminated");
                    }
                } else {
                    log.info("[RefresherHandler] Refresher executor has been successfully shutdown");
                }
            } catch (InterruptedException e) {
                log.warn("[RefresherHandler] Interrupted while waiting for refresher executor to terminate", e);
                refreshExecutor.shutdownNow();
                Thread.currentThread().interrupt(); // 保持线程中断状态
            }
        } else if (refreshExecutor == null) {
            log.debug("[RefresherHandler] Refresher executor is null, nothing to shutdown");
        } else {
            log.debug("[RefresherHandler] Refresher executor is already shutdown");
        }
    }

}
