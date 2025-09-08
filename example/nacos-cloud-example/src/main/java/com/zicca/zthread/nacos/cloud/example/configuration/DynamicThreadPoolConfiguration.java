package com.zicca.zthread.nacos.cloud.example.configuration;

import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.toolkit.ThreadPoolExecutorBuilder;
import com.zicca.zthread.spring.base.DynamicThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

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

}
