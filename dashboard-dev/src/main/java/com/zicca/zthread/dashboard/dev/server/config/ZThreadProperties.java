package com.zicca.zthread.dashboard.dev.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * zthread dashboard 配置文件
 *
 * @author zicca
 */
@Data
@Component
@ConfigurationProperties(prefix = "zthread")
public class ZThreadProperties {

    /**
     * 用户集合
     */
    private List<String> users;

    /**
     * Nacos 命名空间
     */
    private List<String> namespaces;

}
