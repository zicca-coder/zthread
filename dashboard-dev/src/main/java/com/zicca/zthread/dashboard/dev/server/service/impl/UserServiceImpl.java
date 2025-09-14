package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.zicca.zthread.dashboard.dev.server.config.ZThreadProperties;
import com.zicca.zthread.dashboard.dev.server.dto.UserDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author zicca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final ZThreadProperties zThreadProperties;

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String actualAuth = requestParam.getUsername() + ", " + requestParam.getPassword();
        if (!zThreadProperties.getUsers().contains(actualAuth)) {
            throw new RuntimeException("Invalid username or password");
        }

        StpUtil.login(requestParam.getUsername());
        SaSession session = StpUtil.getSession();
        session.set(
                "user",
                JSON.toJSONString(
                        UserDetailRespDTO.builder()
                                .userId("1")
                                .homePath("/")
                                .realName("zicca")
                                .username(requestParam.getUsername())
                                .desc("zqianglee@outlook.com")
                                .avatar("https://avatars.githubusercontent.com/u/212079401?v=4")
                                .build()
                )
        );

        return UserLoginRespDTO.builder()
                .id("1") // 因为用户模块非主要，这里固定写
                .realName("zicca") // 因为用户模块非主要，这里固定写
                .username(requestParam.getUsername())
                .password(requestParam.getPassword()) // 前端用的 https://github.com/vbenjs/vue-vben-admin，为了兼容框架返回。正常登录不需要返回密码
                .accessToken(StpUtil.getTokenValue())
                .build();
    }

    @Override
    public UserDetailRespDTO getUser() {
        return JSON.parseObject(StpUtil.getSession().get("user").toString(), UserDetailRespDTO.class);
    }
}
