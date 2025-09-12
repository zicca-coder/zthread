package com.zicca.zthread.nacos.cloud.example.configuration;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.toolkit.ThreadPoolExecutorBuilder;
import com.zicca.zthread.spring.base.DynamicThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 动态线程池配置
 *
 * @author zicca
 */
@Configuration
public class DynamicThreadPoolConfiguration {


    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor zthreadProducer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("zthread-producer")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("zthread-producer-thread_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor zthreadConsumer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("zthread-consumer")
                .corePoolSize(4)
                .maximumPoolSize(6)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .threadFactory("zthread-consumer-thread_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }

    @Bean("zthreadPoolExecutor")
    @DynamicThreadPool
    public ThreadPoolExecutor zthreadPoolExecutor() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("zthread-executor")
                .corePoolSize(4)
                .maximumPoolSize(6)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE)
                .threadFactory("zthread-executor-thread_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }

    @Bean("threadPoolExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                4,
                6,
                9999L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNamePrefix("normal-executor-thread_").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

}
