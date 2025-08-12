package com.zicca.zthread.spring.base.configuration;

import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import com.zicca.zthread.spring.base.support.ZThreadBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 动态线程池基础 Spring 配置类
 *
 * @author zicca
 */
@Configuration
public class ZThreadBaseConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn("applicationContextHolder")
    public ZThreadBeanPostProcessor zThreadBeanPostProcessor(BootstrapConfigProperties properties) {
        return new ZThreadBeanPostProcessor(properties);
    }




}
