package com.zicca.zthread.dashboard.dev.server.remote.dto;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Nacos 服务集合响应实体
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NacosServiceListRespDTO {

    /**
     * 符合条件的服务的总数
     */
    private Integer totalCount;

    /**
     * 当前页码
     */
    private Integer pageNumber;

    /**
     * 可用页码
     */
    private Integer pagesAvailable;

    /**
     * 服务列表
     */
    private List<NacosServiceRespDTO> pageItems;

}
