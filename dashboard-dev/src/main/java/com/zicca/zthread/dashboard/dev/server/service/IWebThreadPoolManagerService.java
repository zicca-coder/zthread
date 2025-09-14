package com.zicca.zthread.dashboard.dev.server.service;

import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;

import java.util.List;

/**
 * web线程池管理服务
 *
 * @author zicca
 */
public interface IWebThreadPoolManagerService {
    /**
     * 查询线程池集合
     *
     * @param requestParam 请求参数
     * @return 线程池集合
     */
    List<WebThreadPoolDetailRespDTO> listThreadPool(WebThreadPoolListReqDTO requestParam);

    /**
     * 全局修改线程池参数
     *
     * @param requestParam 请求参数
     */
    void updateGlobalThreadPool(WebThreadPoolUpdateReqDTO requestParam);
}
