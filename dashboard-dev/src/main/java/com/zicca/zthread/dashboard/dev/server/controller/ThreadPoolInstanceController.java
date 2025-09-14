package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolStateRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线程池实例控制层
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class ThreadPoolInstanceController {

    private final IThreadPoolInstanceService threadPoolInstanceService;

    /**
     * 获取动态线程池列表
     */
    @GetMapping("/thread-pools/{namespace}/{serviceName}/{threadPoolId}/basic-metrics")
    public Result<List<ThreadPoolBaseMetricsRespDTO>> listBasicMetrics(
            @PathVariable String namespace,
            @PathVariable String serviceName,
            @PathVariable String threadPoolId) {
        return Results.success(threadPoolInstanceService.listBasicMetrics(namespace, serviceName, threadPoolId));
    }

    /**
     * 获取动态线程池的完整运行时状态
     */
    @GetMapping("/thread-pool/{threadPoolId}/{networkAddress}")
    public Result<ThreadPoolStateRespDTO> getRuntimeState(
            @PathVariable String threadPoolId,
            @PathVariable String networkAddress) {
        return Results.success(threadPoolInstanceService.getRuntimeState(threadPoolId, networkAddress));
    }

}
