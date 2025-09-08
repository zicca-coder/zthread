package com.zicca.zthread.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Web 线程池配置变更通知实体
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolConfigChangeDTO {

    /**
     * Web 容器名称
     */
    private String webContainerName;

    /**
     * 环境标识
     */
    private String activeProfile;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 应用节点唯一标识
     */
    private String identify;

    /**
     * 通知接收人
     */
    private String receives;

    /**
     * 配置项集合
     * Key: 配置项名称 (如corePoolSize)
     * Value: 变更前后值对
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * 变更时间
     */
    private String updateTime;

    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        private T before;
        private T after;
    }
}
