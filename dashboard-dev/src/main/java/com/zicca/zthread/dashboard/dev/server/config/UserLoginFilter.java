package com.zicca.zthread.dashboard.dev.server.config;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户登录验证过滤器
 *
 * @author zicca
 */
@Component
@WebFilter(value = "/*", filterName = "userLoginFilter")
public class UserLoginFilter extends OncePerRequestFilter {
    @Value("${zthread.user-login.exclude.interfaces}")
    private String excludeInterfaces;

    @SneakyThrows
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        // 判断是否是需要排除的接口
        if (excludeInterface(requestURI)) {
            // 是需要排除的接口，直接继续请求
            filterChain.doFilter(request, response);
        } else {
            // 非排除的接口，进行登录状态检查
            boolean isLoggedIn = checkUserLoginStatus();
            if (isLoggedIn) {
                // 用户已登录，继续请求
                filterChain.doFilter(request, response);
            } else {
                // 用户未登录，返回未授权的错误码或进行重定向等操作
                returnJson(response, JSON.toJSONString(Results.fail(401, "用户未登录")));
            }
        }
    }

    private boolean excludeInterface(String requestURI) {
        return excludeInterfaces.contains(requestURI);
    }

    private boolean checkUserLoginStatus() {
        return StpUtil.isLogin();
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }
}
