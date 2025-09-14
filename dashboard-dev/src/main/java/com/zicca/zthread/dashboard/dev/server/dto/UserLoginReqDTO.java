package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.Data;

/**
 * 用户登录请求参数实体
 *
 * @author zicca
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
