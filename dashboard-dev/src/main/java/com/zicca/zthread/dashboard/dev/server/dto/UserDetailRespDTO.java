package com.zicca.zthread.dashboard.dev.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户详情响应实体
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailRespDTO {

    /**
     * 用户头像地址
     */
    private String avatar;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户唯一标识ID
     */
    private String userId;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 用户描述或简介
     */
    private String desc;

    /**
     * 用户主页路径（登录后默认跳转页面）
     */
    private String homePath;
}
