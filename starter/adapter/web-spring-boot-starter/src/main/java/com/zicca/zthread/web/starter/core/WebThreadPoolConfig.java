package com.zicca.zthread.web.starter.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Web 容器线程池参数动态变更配置类
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolConfig {

    /**
     * 线程池核心线程数
     */
    private Integer corePoolSize;

    /**
     * 线程池最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 空闲线程最大保活时间
     */
    private Long keepAliveTime;
}
