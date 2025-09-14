package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.ProjectInfoRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目控制器
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class ProjectController {

    private final IProjectService projectService;

    @GetMapping("/projects")
    public Result<List<ProjectInfoRespDTO>> listProjects() {
        return Results.success(projectService.listProject());
    }

}
