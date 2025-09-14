package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;
import com.zicca.zthread.dashboard.dev.server.service.IThreadPoolManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线程池管理控制层
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class ThreadPoolManagerController {

    private final IThreadPoolManagerService threadPoolManagerService;

    /**
     * 查询线程池集合
     */
    @GetMapping("/thread-pools")
    public Result<List<ThreadPoolDetailRespDTO>> listThreadPool(ThreadPoolListReqDTO requestParam) {
        return Results.success(threadPoolManagerService.listThreadPool(requestParam));
    }

    /**
     * 更新线程池
     */
    @PutMapping("/thread-pool")
    public Result<Void> updateGlobalThreadPool(@RequestBody @Valid ThreadPoolUpdateReqDTO requestParam) {
        threadPoolManagerService.updateGlobalThreadPool(requestParam);
        return Results.success();
    }

}
