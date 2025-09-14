package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.zicca.zthread.dashboard.dev.server.service.IWebThreadPoolManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * web线程池管理
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class WebThreadPoolManagerController {

    private final IWebThreadPoolManagerService webThreadPoolManagerService;

    /**
     * 查询线程池集合
     */
    @GetMapping("/web/thread-pools")
    public Result<List<WebThreadPoolDetailRespDTO>> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        return Results.success(webThreadPoolManagerService.listThreadPool(requestParam));
    }

    /**
     * 更新线程池
     */
    @PutMapping("/web/thread-pool")
    public Result<Void> updateGlobalThreadPool(@RequestBody @Valid WebThreadPoolUpdateReqDTO requestParam) {
        webThreadPoolManagerService.updateGlobalThreadPool(requestParam);
        return Results.success();
    }
}
