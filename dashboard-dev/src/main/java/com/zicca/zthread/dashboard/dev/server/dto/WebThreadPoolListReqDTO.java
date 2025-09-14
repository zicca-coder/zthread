package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.Data;

/**
 * Web 线程池控制台查询请求实体
 *
 * @author zicca
 */
@Data
public class WebThreadPoolListReqDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String serviceName;
}
