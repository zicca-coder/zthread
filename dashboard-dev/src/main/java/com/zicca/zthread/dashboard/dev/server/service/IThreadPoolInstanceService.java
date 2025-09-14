package com.zicca.zthread.dashboard.dev.server.service;

import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolStateRespDTO;

import java.util.List;

/**
 * 线程池实例服务
 *
 * @author zicca
 */
public interface IThreadPoolInstanceService {

    /**
     * 获取线程池的轻量级运行指标（无锁，适合高频调用）
     *
     * @param namespace    命名空间
     * @param serviceName  服务名称
     * @param threadPoolId 线程池唯一标识
     * @return WebThreadPoolState 的简化视图，仅包含关键运行时指标
     */
    List<ThreadPoolBaseMetricsRespDTO> listBasicMetrics(String namespace, String serviceName, String threadPoolId);

    /**
     * 获取线程池的完整运行时状态（可能涉及锁操作，不建议高频调用）
     *
     * @param threadPoolId   线程池唯一标识
     * @param networkAddress 网络地址
     * @return 完整的线程池运行状态信息
     */
    ThreadPoolStateRespDTO getRuntimeState(String threadPoolId, String networkAddress);

}
