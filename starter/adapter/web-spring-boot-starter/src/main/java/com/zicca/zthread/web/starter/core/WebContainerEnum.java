package com.zicca.zthread.web.starter.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Web 容器类型枚举
 *
 * @author zicca
 */
@RequiredArgsConstructor
public enum WebContainerEnum {

    TOMCAT("Tomcat"),

    JETTY("Jetty"),

    UNDERTOW("Undertow");

    @Getter
    private final String name;

    @Override
    public String toString() {
        return getName();
    }
}
