package com.zicca.zthread.dashboard.dev.starter.controller;

import com.zicca.zthread.dashboard.dev.starter.core.Result;
import com.zicca.zthread.dashboard.dev.starter.core.Results;
import com.zicca.zthread.dashboard.dev.starter.dto.WebThreadPoolDashBoardDevBasicMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.starter.dto.WebThreadPoolDashBoardDevRespDTO;
import com.zicca.zthread.dashboard.dev.starter.service.IWebThreadPoolService;
import com.zicca.zthread.web.starter.core.WebThreadPoolBaseMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * web线程池控制器
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/web/thread-pool")
public class WebThreadPoolController {

    private final IWebThreadPoolService webThreadPoolService;

    @GetMapping
    public Result<WebThreadPoolDashBoardDevRespDTO> getRuntimeInfo() {
        return Results.success(webThreadPoolService.getRuntimeInfo());
    }

    @GetMapping("/basic-metrics")
    public Result<WebThreadPoolDashBoardDevBasicMetricsRespDTO> getBasicMetrics() {
        return Results.success(webThreadPoolService.getBasicMetrics());
    }



}
