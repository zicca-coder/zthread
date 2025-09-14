package com.zicca.zthread.dashboard.dev.server.service;

import com.zicca.zthread.dashboard.dev.server.dto.UserDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginRespDTO;

/**
 * 用户服务
 *
 * @author zicca
 */
public interface IUserService {
    /**
     * 用户登录
     *
     * @param requestParam 用户名、密码
     * @return 用户登录返回信息
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 获取用户明细信息
     *
     * @return 用户明细信息
     */
    UserDetailRespDTO getUser();
}
