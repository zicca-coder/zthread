package com.zicca.zthread.dashboard.dev.starter.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.Ipv4Util;
import com.zicca.zthread.dashboard.dev.starter.dto.WebThreadPoolDashBoardDevBasicMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.starter.dto.WebThreadPoolDashBoardDevRespDTO;
import com.zicca.zthread.web.starter.core.WebThreadPoolBaseMetrics;
import com.zicca.zthread.web.starter.core.WebThreadPoolState;
import com.zicca.zthread.web.starter.core.executor.WebThreadPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import static com.zicca.zthread.dashboard.dev.starter.toolkit.MemoryUtil.getFreeMemory;
import static com.zicca.zthread.dashboard.dev.starter.toolkit.MemoryUtil.getMemoryProportion;

/**
 * Web 线程池接口
 * @author zicca
 */
@RequiredArgsConstructor
public class IWebThreadPoolService {

    private final WebThreadPoolService webThreadPoolService;

    @Value("${server.port}")
    private String port;

    @Value("${spring.profiles.active:UNKONWN}")
    private String activeProfile;

    /**
     * 获取Web线程池基础指标信息
     *
     * @return WebThreadPoolBaseMetrics 基础指标信息对象，包含活跃配置、网络地址和Web容器名称等信息
     */
    public WebThreadPoolDashBoardDevBasicMetricsRespDTO getBasicMetrics() {
        WebThreadPoolBaseMetrics baseMetrics = webThreadPoolService.getBaseMetrics();
        baseMetrics.setActiveProfile(activeProfile);
        baseMetrics.setNetworkAddress(Ipv4Util.LOCAL_IP + ":" + port);
        baseMetrics.setWebContainerName(webThreadPoolService.getWebContainerType().name());
        return BeanUtil.toBean(baseMetrics, WebThreadPoolDashBoardDevBasicMetricsRespDTO.class);
    }

    /**
     * 获取Web线程池运行时信息
     *
     * @return WebThreadPoolDashBoardDevRespDTO Web线程池仪表板响应数据传输对象，包含线程池的运行时状态信息
     */
    public WebThreadPoolDashBoardDevRespDTO getRuntimeInfo() {
        WebThreadPoolState runtimeState = webThreadPoolService.getRuntimeState();
        WebThreadPoolDashBoardDevRespDTO respDTO = BeanUtil.toBean(runtimeState, WebThreadPoolDashBoardDevRespDTO.class);
        respDTO.setCurrentTime(DateUtil.now())
                .setActiveProfile(activeProfile.toUpperCase())
                .setIp(Ipv4Util.LOCAL_IP)
                .setWebContainerName(webThreadPoolService.getWebContainerType().name())
                .setPort(port)
                .setCurrentLoad((int) Math.round((runtimeState.getActivePoolSize() * 100.0) / runtimeState.getMaximumPoolSize()) + "%")
                .setPeakLoad((int) Math.round((runtimeState.getLargestPoolSize() * 100.0) / runtimeState.getMaximumPoolSize()) + "%")
                .setFreeMemory(getFreeMemory())
                .setMemoryUsagePercentage(getMemoryProportion())
                .setStatus(webThreadPoolService.getRunningStatus());
        return respDTO;
    }

}
