package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IWebThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web线程池实例控制层
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class WebThreadPoolInstanceController {

    private final IWebThreadPoolInstanceService webThreadPoolInstanceService;

    /**
     * 获取线程池列表
     */
    @GetMapping("/web/thread-pools/{namespace}/{serviceName}/basic-metrics")
    public Result<List<WebThreadPoolBaseMetricsRespDTO>> listBasicMetrics(
            @PathVariable String namespace,
            @PathVariable String serviceName) {
        return Results.success(webThreadPoolInstanceService.listBasicMetrics(namespace, serviceName));
    }

    /**
     * 获取线程池的完整运行时状态
     */
    @GetMapping("/web/thread-pool/{networkAddress}")
    public Result<WebThreadPoolStateRespDTO> getRuntimeState(@PathVariable String networkAddress) {
        return Results.success(webThreadPoolInstanceService.getRuntimeState(networkAddress));
    }
}
