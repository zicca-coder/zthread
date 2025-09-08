package com.zicca.zthread.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 线程池配置变更通知实体
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadPoolConfigChangeDTO {

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

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
     * 阻塞队列类型
     */
    private String workQueue;

    /**
     * 配置项集合
     * key: 配置项名称（如 corePoolSize）
     * value: 变更前后值
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * 更新时间
     */
    private String updateTime;


    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        private T before;
        private T after;
    }

}
