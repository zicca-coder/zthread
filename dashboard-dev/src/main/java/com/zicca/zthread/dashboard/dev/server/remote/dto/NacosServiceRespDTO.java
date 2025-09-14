package com.zicca.zthread.dashboard.dev.server.remote.dto;

import lombok.Data;

import java.util.Map;

/**
 * Nacos 服务明细实体
 *
 * @author zicca
 */
@Data
public class NacosServiceRespDTO {

    /**
     * 实例 ID
     */
    private String instanceId;

    /**
     * 实例 IP
     */
    private String ip;

    /**
     * 实例端口
     */
    private Integer port;

    /**
     * 实例权重
     */
    private Double weight;

    /**
     * 实例是否健康
     */
    private Boolean healthy;

    /**
     * 实例是否已上线
     */
    private Boolean enabled;

    /**
     * 实例是否临时
     */
    private Boolean ephemeral;

    /**
     * 实例所属集群
     */
    private String clusterName;

    /**
     * 实例所属服务
     */
    private String serviceName;

    /**
     *
     */
    Map<String, String> metadata;

}
