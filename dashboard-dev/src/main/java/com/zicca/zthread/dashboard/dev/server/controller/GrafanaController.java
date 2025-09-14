package com.zicca.zthread.dashboard.dev.server.controller;

import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.common.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Grafana 看板控制器
 *
 * @author zicca
 */
@Slf4j
@RestController
public class GrafanaController {

    @Value("${zthread.grafana.url}")
    private String grafanaUrl;

    /**
     * 控制台获取 Grafana 预览地址
     */
    @GetMapping("/ap/zthread-dashboard/grafana")
    public Result<String> getGrafanaUrl() {
        return Results.success(grafanaUrl);
    }

}
