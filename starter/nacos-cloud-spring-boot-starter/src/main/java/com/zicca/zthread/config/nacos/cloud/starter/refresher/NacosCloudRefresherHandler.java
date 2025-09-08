package com.zicca.zthread.config.nacos.cloud.starter.refresher;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.zicca.zthread.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
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
import java.util.Locale;
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
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {

    private final ConfigService configService;

    public NacosCloudRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        super(properties);
        this.configService = configService;
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
                        return ThreadPoolExecutorBuilder.builder()
                                .corePoolSize(1)                    // 核心线程数为1
                                .maximumPoolSize(1)                 // 最大线程数为1
                                .keepAliveTime(9999L)              // 线程空闲存活时间
                                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)  // 使用同步队列
                                .threadFactory("cloud-nacos-refresher-thread_")          // 线程名称前缀
                                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy()) // 拒绝策略：调用者运行
                                .build();
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 当配置发生变化时，触发线程池属性刷新
                        refreshThreadPoolProperties(configInfo);
                    }
                }
        );
        
        // 记录监听器注册成功的日志
        log.info("Dynamic thread pool refresher, add nacos cloud listener success. data-id: {}, group: {}", 
                nacosConfig.getDataId(), nacosConfig.getGroup());
    }

}
