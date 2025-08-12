package com.zicca.zthread.spring.base.enable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标记配置类
 * 用于在 Spring 容器中注入标记对象，作为是否启用动态线程池的条件判断依据
 *
 * @author zicca
 */
@Configuration
public class MarkerConfiguration {

    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    public class Marker {

    }

}
