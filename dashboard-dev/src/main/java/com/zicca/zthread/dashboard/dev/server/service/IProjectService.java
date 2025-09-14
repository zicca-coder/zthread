package com.zicca.zthread.dashboard.dev.server.service;

import com.zicca.zthread.dashboard.dev.server.dto.ProjectInfoRespDTO;

import java.util.List;

/**
 * 项目服务
 *
 * @author zicca
 */
public interface IProjectService {


    /**
     * 获取项目列表
     *
     * @return 项目列表
     */
    List<ProjectInfoRespDTO> listProject();

}
