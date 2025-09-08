package com.zicca.zthread.config.common.starter.refresher;

import com.zicca.zthread.core.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置中心刷新线程池参数变更事件
 * <p>
 * 该事件用于在配置中心的线程池配置发生变更时，通知应用进行相应的线程池调整。
 * 通过继承 Spring 的 ApplicationEvent，可以方便地在 Spring 容器中发布和监听此事件。
 * </p>
 *
 * @author zicca
 */
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {
    
    /**
     * Bootstrap 配置属性，包含线程池相关的最新配置信息
     */
    @Getter
    @Setter
    private BootstrapConfigProperties bootstrapConfigProperties;
    
    /**
     * 构造方法
     *
     * @param source 事件源，通常是事件发布者本身
     * @param bootstrapConfigProperties 包含更新后线程池配置的 BootstrapConfigProperties 对象
     */
    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties bootstrapConfigProperties) {
        super(source);
        this.bootstrapConfigProperties = bootstrapConfigProperties;
    }
}
