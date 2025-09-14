package com.zicca.zthread.dashboard.dev.server.remote.dto;

import lombok.Data;

/**
 * Nacos 配置详情响应实体
 *
 * @author zicca
 */
@Data
public class NacosConfigDetailRespDTO {

    /**
     * 配置 ID（唯一标识）
     */
    private String id;

    /**
     * 配置的 Data ID
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
     * 配置的完整内容（YAML、JSON 等）
     */
    private String content;

    /**
     * 配置描述
     */
    private String desc;

    /**
     * 配置内容的 MD5 值，用于校验变更
     */
    private String md5;

    /**
     * 配置的标签
     */
    private String configTags;

    /**
     * 加密配置内容的密钥，使用配置加密插件时存在
     */
    private String encryptedDataKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 配置类型（yaml、properties 等）
     */
    private String type;

    /**
     * 创建时间（时间戳，单位毫秒）
     */
    private Long createTime;

    /**
     * 修改时间（时间戳，单位毫秒）
     */
    private Long modifyTime;

    /**
     * 创建用户（若有记录）
     */
    private String createUser;

    /**
     * 创建 IP（可能是本地或远程地址）
     */
    private String createIp;
}
