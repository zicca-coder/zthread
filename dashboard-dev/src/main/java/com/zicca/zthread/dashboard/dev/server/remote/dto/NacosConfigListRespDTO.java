package com.zicca.zthread.dashboard.dev.server.remote.dto;

import lombok.Data;

import java.util.List;

/**
 * Nacos 配置集合响应实体
 *
 * @author zicca
 */
@Data
public class NacosConfigListRespDTO {

    /**
     * 总配置项数量
     */
    private Integer totalCount;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNumber;

    /**
     * 总页数
     */
    private Integer pagesAvailable;

    /**
     * 当前页的配置项列表
     */
    private List<NacosConfigRespDTO> pageItems;

}
