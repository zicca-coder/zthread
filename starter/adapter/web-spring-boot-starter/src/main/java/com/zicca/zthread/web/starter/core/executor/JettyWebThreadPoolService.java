package com.zicca.zthread.web.starter.core.executor;

import cn.hutool.core.util.ReflectUtil;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.constant.Constants;
import com.zicca.zthread.web.starter.core.WebContainerEnum;
import com.zicca.zthread.web.starter.core.WebThreadPoolBaseMetrics;
import com.zicca.zthread.web.starter.core.WebThreadPoolConfig;
import com.zicca.zthread.web.starter.core.WebThreadPoolState;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import static com.zicca.zthread.core.constant.Constants.CHANGE_JETTY_THREAD_POOL_TEXT;
import static com.zicca.zthread.web.starter.core.WebContainerEnum.JETTY;

/**
 * Jetty 线程池接口
 *
 * @author zicca
 */
@Slf4j
public class JettyWebThreadPoolService extends AbstractWebThreadPoolService {
    @Override
    protected Executor getExecutor(WebServer webServer) {
        return ((JettyWebServer) webServer).getServer().getThreadPool();
    }

    public void init() {
        BootstrapConfigProperties.WebThreadPoolExecutorConfig config = BootstrapConfigProperties.getInstance().getWeb();
        if (Objects.isNull(config)) {
            return;
        }
        log.debug("Initialize Web ThreadPool from remote config center: {}", config);
        QueuedThreadPool jettyExecutor = (QueuedThreadPool) executor;
        int originalCorePoolSize = jettyExecutor.getMinThreads();
        int originalMaximumPoolSize = jettyExecutor.getMaxThreads();
        long originalKeepAliveTime = jettyExecutor.getIdleTimeout();

        if (config.getCorePoolSize() > originalMaximumPoolSize) {
            jettyExecutor.setMaxThreads(config.getMaximumPoolSize());
            jettyExecutor.setMinThreads(config.getCorePoolSize());
        } else {
            jettyExecutor.setMinThreads(config.getCorePoolSize());
            jettyExecutor.setMaxThreads(config.getMaximumPoolSize());
        }
        jettyExecutor.setIdleTimeout(config.getKeepAliveTime().intValue());
    }

    @Override
    public void updateThreadPool(WebThreadPoolConfig config) {
        try {
            QueuedThreadPool jettyExecutor = (QueuedThreadPool) executor;
            int originalCorePoolSize = jettyExecutor.getMinThreads();
            int originalMaximumPoolSize = jettyExecutor.getMaxThreads();
            long originalKeepAliveTime = jettyExecutor.getIdleTimeout();

            if (config.getCorePoolSize() > originalMaximumPoolSize) {
                jettyExecutor.setMaxThreads(config.getMaximumPoolSize());
                jettyExecutor.setMinThreads(config.getCorePoolSize());
            } else {
                jettyExecutor.setMinThreads(config.getCorePoolSize());
                jettyExecutor.setMaxThreads(config.getMaximumPoolSize());
            }
            jettyExecutor.setIdleTimeout(config.getKeepAliveTime().intValue());

            log.info(CHANGE_JETTY_THREAD_POOL_TEXT,
                    String.format(Constants.CHANGE_DELIMITER, originalCorePoolSize, config.getCorePoolSize()),
                    String.format(Constants.CHANGE_DELIMITER, originalMaximumPoolSize, config.getMaximumPoolSize()),
                    String.format(Constants.CHANGE_DELIMITER, originalKeepAliveTime, config.getKeepAliveTime()));
        } catch (Exception ex) {
            log.error("Failed to modify the Jetty thread pool parameter.", ex);
        }
    }

    @Override
    public WebThreadPoolBaseMetrics getBaseMetrics() {
        QueuedThreadPool jettyExecutor = (QueuedThreadPool) executor;
        int corePoolSize = jettyExecutor.getMinThreads();
        int maximumPoolSize = jettyExecutor.getMaxThreads();
        long keepAliveTime = jettyExecutor.getIdleTimeout();

        BlockingQueue jobs = (BlockingQueue) ReflectUtil.getFieldValue(jettyExecutor, "_jobs");
        int blockingQueueSize = jettyExecutor.getQueueSize();
        int remainingCapacity = jobs.remainingCapacity();
        int queueCapacity = blockingQueueSize + remainingCapacity;
        String rejectedExecutionHandlerName = "JettyRejectedExecutionHandler";

        return WebThreadPoolBaseMetrics.builder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .keepAliveTime(keepAliveTime)
                .workQueueName(jobs.getClass().getSimpleName())
                .workQueueSize(blockingQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(queueCapacity)
                .rejectedHandlerName(rejectedExecutionHandlerName)
                .build();
    }

    @Override
    public WebThreadPoolState getRuntimeState() {
        QueuedThreadPool jettyExecutor = (QueuedThreadPool) executor;
        int corePoolSize = jettyExecutor.getMinThreads();
        int maximumPoolSize = jettyExecutor.getMinThreads();
        int activeCount = jettyExecutor.getBusyThreads();
        int currentPoolSize = jettyExecutor.getThreads();
        long keepAliveTime = jettyExecutor.getIdleTimeout();

        BlockingQueue jobs = (BlockingQueue) ReflectUtil.getFieldValue(jettyExecutor, "_jobs");
        int blockingQueueSize = jettyExecutor.getQueueSize();
        int remainingCapacity = jobs.remainingCapacity();
        int queueCapacity = blockingQueueSize + remainingCapacity;
        String rejectedExecutionHandlerName = "JettyRejectedExecutionHandler";
        // 相对于 Tomcat 来说，Jetty 的线程池个性化更多，这会导致 WebThreadPoolState 并不完整
        return WebThreadPoolState.builder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .activePoolSize(activeCount)
                .currentPoolSize(currentPoolSize)
                .keepAliveTime(keepAliveTime)
                .workQueueName(jobs.getClass().getSimpleName())
                .workQueueSize(blockingQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(queueCapacity)
                .rejectedHandlerName(rejectedExecutionHandlerName)
                .build();
    }

    @Override
    public WebContainerEnum getWebContainerType() {
        return JETTY;
    }
}
