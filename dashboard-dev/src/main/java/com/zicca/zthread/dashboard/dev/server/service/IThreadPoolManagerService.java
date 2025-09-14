package com.zicca.zthread.dashboard.dev.server.service;

import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;

import java.util.List;

/**
 * 线程池管理服务
 *
 * @author zicca
 */
public interface IThreadPoolManagerService {
    /**
     * 查询线程池集合
     *
     * @param requestParam 请求参数
     * @return 线程池集合
     */
    List<ThreadPoolDetailRespDTO> listThreadPool(ThreadPoolListReqDTO requestParam);

    /**
     * 全局修改线程池参数
     *
     * @param requestParam 请求参数
     */
    void updateGlobalThreadPool(ThreadPoolUpdateReqDTO requestParam);
}
