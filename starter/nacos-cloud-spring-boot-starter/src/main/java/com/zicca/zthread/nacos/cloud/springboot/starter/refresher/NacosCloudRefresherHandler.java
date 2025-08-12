package com.zicca.zthread.nacos.cloud.springboot.starter.refresher;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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

    public void refreshThreadPoolProperties(String configInfo) throws IOException {
        Map<Object, Object> configInfoMap = ConfigParserHandler.getInstance().parseConfig(configInfo, properties.getConfigFileType());
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);
        Binder binder = new Binder(source);

        BootstrapConfigProperties refresherProperties = binder.bind(BootstrapConfigProperties.PREFIX, Bindable.ofInstance(properties)).get();
        log.info("Latest updated configuration: \n{}", configInfo);
        log.info("Java configuration object binding: \n{}", JSON.toJSONString(refresherProperties));
        // ...... 检测线程池参数是否变更，如果已变更则进行更新

    }
}
