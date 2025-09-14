package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应实体
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRespDTO {

    /**
     * 用户唯一标识ID
     */
    private String id;

    /**
     * 登录后生成的访问令牌（Access Token）
     */
    private String accessToken;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;
}
