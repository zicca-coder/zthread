package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import com.zicca.zthread.dashboard.dev.server.dto.UserDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.UserLoginRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理
 *
 * @author zicca
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zthread-dashboard")
public class UserController {

    private final IUserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/auth/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 查询用户信息
     */
    @GetMapping("/user")
    public Result<UserDetailRespDTO> getUser() {
        return Results.success(userService.getUser());
    }
}
