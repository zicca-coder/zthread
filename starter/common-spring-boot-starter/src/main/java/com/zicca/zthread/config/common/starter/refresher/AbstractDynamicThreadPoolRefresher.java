package com.zicca.zthread.config.common.starter.refresher;

import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.parser.ConfigParserHandler;
import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

/**
 * 基于模板方法模式抽象动态线程池刷新逻辑
 *
 * @author zicca
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDynamicThreadPoolRefresher implements ApplicationRunner {

    protected final BootstrapConfigProperties properties;

    /**
     * 注册配置变更监听器，由子类实现具体逻辑
     *
     * @throws Exception
     */
    protected abstract void registerListener() throws Exception;

    /**
     * 默认空实现，子类可以按需覆盖
     */
    protected void beforeRegister() {

    }

    /**
     * 默认空实现，子类可以按需覆盖
     */
    protected void afterRegister() {

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        beforeRegister();
        registerListener();
        afterRegister();
    }

    @SneakyThrows
    public void refreshThreadPoolProperties(String configInfo) {
        Map<Object, Object> configInfoMap = ConfigParserHandler.getInstance().parseConfig(configInfo, properties.getConfigFileType());
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);
        Binder binder = new Binder(source);
        BootstrapConfigProperties refresherProperties = binder.bind(BootstrapConfigProperties.PREFIX, Bindable.ofInstance(properties)).get();

        ApplicationContextHolder.publishEvent(new ThreadPoolConfigUpdateEvent(this, refresherProperties));
    }

}
