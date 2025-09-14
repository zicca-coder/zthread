package com.zicca.zthread.dashboard.dev.starter.controller;

import com.zicca.zthread.dashboard.dev.starter.core.Result;
import com.zicca.zthread.dashboard.dev.starter.core.Results;
import com.zicca.zthread.dashboard.dev.starter.dto.ThreadPoolDashBoardDevBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.starter.dto.ThreadPoolDashBoardDevRespDTO;
import com.zicca.zthread.dashboard.dev.starter.service.IDynamicThreadPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态线程池控制器
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dynamic/thread-pool")
public class DynamicThreadPoolController {

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    @GetMapping("/{threadPoolId}/basic-metrics")
    public Result<ThreadPoolDashBoardDevBaseMetricsRespDTO> getBasicMetrics(@PathVariable String threadPoolId) {
        return Results.success(dynamicThreadPoolService.getBasicMetrics(threadPoolId));
    }

    @GetMapping("/{threadPoolId}")
    public Result<ThreadPoolDashBoardDevRespDTO> getRuntimeInfo(@PathVariable String threadPoolId) {
        return Results.success(dynamicThreadPoolService.getRuntimeInfo(threadPoolId));
    }

}
