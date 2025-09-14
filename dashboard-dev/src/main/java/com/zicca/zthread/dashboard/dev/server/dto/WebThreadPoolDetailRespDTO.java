package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Web 线程池控制台开发测试响应实体
 *
 * @author zicca
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class WebThreadPoolDetailRespDTO {

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
     * Web 容器名称
     */
    private String webContainerName;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 通知配置
     */
    private NotifyConfig notify;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         */
        private String receives;
    }
}
