package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目接口返回实体类
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInfoRespDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 项目名 | 服务名
     */
    private String serviceName;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 线程池数量
     */
    private Integer threadPoolCount;

    /**
     * 是否有web线程池
     */
    private Boolean hasWebThreadPool;

    /**
     * 修改时间
     */
    private String updateTime;

}
