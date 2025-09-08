package com.zicca.zthread.config.common.starter.configuration;

import com.zicca.zthread.config.common.starter.refresher.DynamicThreadPoolRefreshListener;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.notification.service.NotifierDispatcher;
import com.zicca.zthread.spring.base.configuration.ZThreadBaseConfiguration;
import com.zicca.zthread.spring.base.enable.MarkerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * 基于配置中心的公共自动装配配置
 *
 * @author zicca
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@Import(ZThreadBaseConfiguration.class)
@AutoConfigureAfter(ZThreadBaseConfiguration.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(Environment environment) {
        // 使用 Spring 的 Binder.ger(environment) 获取属性绑定器
        // 通过 bind() 方法将环境钟的配置属性绑定到 BootstrapConfigProperties 对象中
        // BootstrapConfigProperties.PREFIX 为配置属性的前缀，用于匹配配置属性
        // Bindable.of(BootstrapConfigProperties.class)指定要绑定的目标类型
        BootstrapConfigProperties bootstrapConfigProperties = Binder.get(environment)
                .bind(BootstrapConfigProperties.PREFIX, Bindable.of(BootstrapConfigProperties.class))
                .get();
        // 将创建的实例设置为单例
        // 这种方式允许在应用程序的其他地方通过静态方法访问配置属性
        BootstrapConfigProperties.setInstance(bootstrapConfigProperties);
        return bootstrapConfigProperties;
    }

    @Bean
    public DynamicThreadPoolRefreshListener dynamicThreadPoolRefreshListener(NotifierDispatcher notifierDispatcher) {
        return new DynamicThreadPoolRefreshListener(notifierDispatcher);
    }

}
