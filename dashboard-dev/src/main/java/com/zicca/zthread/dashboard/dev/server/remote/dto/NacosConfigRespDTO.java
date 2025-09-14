package com.zicca.zthread.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 配置明细响应实体
 *
 * @author zicca
 */
@Data
public class NacosConfigRespDTO {

    /**
     * 配置 ID
     */
    private String dataId;

    /**
     * 配置所属分组，例如：DEFAULT_GROUP
     */
    private String groupName;

    /**
     * 命名空间 ID
     */
    private String namespaceId;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 配置类型
     */
    private String type;
}
