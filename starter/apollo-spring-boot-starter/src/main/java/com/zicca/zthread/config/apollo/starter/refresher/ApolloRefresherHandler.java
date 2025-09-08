package com.zicca.zthread.config.apollo.starter.refresher;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.zicca.zthread.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorHolder;
import com.zicca.zthread.core.executor.ThreadPoolExecutorProperties;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.executor.support.RejectedPolicyTypeEnum;
import com.zicca.zthread.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import com.zicca.zthread.core.parser.ConfigParserHandler;
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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static com.zicca.zthread.core.constant.Constants.CHANGE_DELIMITER;
import static com.zicca.zthread.core.constant.Constants.CHANGE_THREAD_POOL_TEXT;

/**
 * Apollo 配置中心刷新处理器
 *
 * @author zicca
 */
@Slf4j
public class ApolloRefresherHandler extends AbstractDynamicThreadPoolRefresher {


    public ApolloRefresherHandler(BootstrapConfigProperties properties) {
        super(properties);
    }


    /**
     * 注册 Apollo 配置监听器
     * <p>
     * 该方法用于监听 Apollo 配置中心的配置变更，并在配置变更时刷新线程池配置。
     *
     * @throws Exception 如果注册监听器过程中发生异常
     */
    @Override
    protected void registerListener() throws Exception {
        // 获取 Apollo 配置
        BootstrapConfigProperties.ApolloConfig apolloConfig = properties.getApollo();
        // 拆分命名空间配置，支持多个命名空间
        String[] apolloNamespaces = apolloConfig.getNamespace().split(",");

        // 获取第一个命名空间作为主命名空间
        String namespace = apolloNamespaces[0];
        // 获取配置文件类型（如 properties、yaml 等）
        String configFileType = properties.getConfigFileType().getValue();
        // 构造完整的配置标识符并获取 Apollo Config 实例
        Config config = ConfigService.getConfig(String.format("%s.%s", namespace, properties.getConfigFileType().getValue()));

        // 创建配置变更监听器
        ConfigChangeListener configChangeListener = createConfigChangeListener(namespace, configFileType);
        // 注册监听器到 Apollo Config 实例
        config.addChangeListener(configChangeListener);

        // 记录日志，表示监听器注册成功
        log.info("Dynamic thread pool refresher, add apollo listener success. namespace: {}", namespace);
    }

    /**
     * 创建 Apollo 配置变更监听器
     *
     * @param namespace       命名空间
     * @param configFileType  配置文件类型
     * @return 配置变更监听器
     */
    private ConfigChangeListener createConfigChangeListener(String namespace, String configFileType) {
        return configChangeEvent -> {
            // 去除文件类型后缀，获取真实的命名空间名称
            String namespaceItem = namespace.replace("." + configFileType, "");
            // 根据文件类型获取配置文件格式
            ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(configFileType);
            // 获取最新的配置文件内容
            ConfigFile configFile = ConfigService.getConfigFile(namespaceItem, configFileFormat);
            // 刷新线程池配置
            refreshThreadPoolProperties(configFile.getContent());
        };
    }

}
