package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 线程池控制台开发测试响应实体
 *
 * @author zicca
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolDetailRespDTO {
    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 数据 ID
     */
    private String dataId;

    /**
     * 分组标识
     */
    private String group;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 阻塞队列类型
     */
    private String workQueue;

    /**
     * 拒绝策略类型
     */
    private String rejectedHandler;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime;

    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * 通知配置
     */
    private NotifyConfig notify;

    /**
     * 报警配置，默认设置
     */
    @Builder.Default
    private AlarmConfig alarm = new AlarmConfig();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         */
        private String receives;

        /**
         * 告警间隔，单位分钟
         */
        private Integer interval = 5;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmConfig {

        /**
         * 默认开启报警配配置
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 队列阈值
         */
        private Integer queueThreshold = 80;

        /**
         * 活跃线程阈值
         */
        private Integer activeThreshold = 80;
    }
}
