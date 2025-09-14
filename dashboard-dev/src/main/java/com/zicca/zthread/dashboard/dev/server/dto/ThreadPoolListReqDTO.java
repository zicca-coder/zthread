package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.Data;

/**
 * 线程池控制台查询请求实体
 *
 * @author zicca
 */
@Data
public class ThreadPoolListReqDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String serviceName;
}
