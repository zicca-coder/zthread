package com.zicca.zthread.web.starter.core;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.zicca.zthread.config.common.starter.refresher.ThreadPoolConfigUpdateEvent;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.notification.dto.WebThreadPoolConfigChangeDTO;
import com.zicca.zthread.core.notification.service.NotifierDispatcher;
import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import com.zicca.zthread.web.starter.core.executor.WebThreadPoolService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Web 线程池监听配置中心刷新事件
 *
 * @author zicca
 */
@RequiredArgsConstructor
public class WebThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {

    private final WebThreadPoolService webThreadPoolService;
    private final NotifierDispatcher notifierDispatcher;

    /**
     * 监听线程池配置更新事件，当检测到 Web 线程池配置发生变更时，动态更新线程池参数并发送通知
     *
     * @param event 线程池配置更新事件，包含最新的配置信息
     */
    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {
        // 获取事件中的 Web 线程池配置
        BootstrapConfigProperties.WebThreadPoolExecutorConfig webExecutorConfig = event.getBootstrapConfigProperties().getWeb();
        // 若配置为空，则直接返回，不进行后续处理
        if (Objects.isNull(webExecutorConfig)) {
            return;
        }

        // 获取当前 Web 线程池的基础运行指标，用于与新配置进行比较
        WebThreadPoolBaseMetrics basicMetrics = webThreadPoolService.getBaseMetrics();

        // 比较核心线程数、最大线程数和空闲线程存活时间是否发生变化
        if (!Objects.equals(basicMetrics.getCorePoolSize(), webExecutorConfig.getCorePoolSize())
                || !Objects.equals(basicMetrics.getMaximumPoolSize(), webExecutorConfig.getMaximumPoolSize())
                || !Objects.equals(basicMetrics.getKeepAliveTime(), webExecutorConfig.getKeepAliveTime())) {
            
            // 若配置发生变化，则将新配置转换为 WebThreadPoolConfig 并更新线程池
            webThreadPoolService.updateThreadPool(BeanUtil.toBean(webExecutorConfig, WebThreadPoolConfig.class));

            // 发送 Web 线程池配置变更通知
            if (BootstrapConfigProperties.getInstance().getNotifyPlatforms().getEnable()) {
                sendWebThreadPoolConfigChangeMessage(basicMetrics, webExecutorConfig);
            }
        }
    }

    @SneakyThrows
    public void sendWebThreadPoolConfigChangeMessage(WebThreadPoolBaseMetrics originalProperties,
                                                     BootstrapConfigProperties.WebThreadPoolExecutorConfig remoteProperties) {
        Environment environment = ApplicationContextHolder.getBean(Environment.class);
        String applicationName = environment.getProperty("spring.application.name");
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");

        Map<String, WebThreadPoolConfigChangeDTO.ChangePair<?>> changes = new HashMap<>();
        changes.put("corePoolSize", new WebThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()));
        changes.put("maximumPoolSize", new WebThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()));
        changes.put("keepAliveTime", new WebThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()));

        WebThreadPoolConfigChangeDTO configChangeDTO = WebThreadPoolConfigChangeDTO.builder()
                .activeProfile(activeProfile)
                .identify(InetAddress.getLocalHost().getHostAddress())
                .applicationName(applicationName)
                .webContainerName(webThreadPoolService.getWebContainerType().getName())
                .receives(remoteProperties.getNotify().getReceives())
                .changes(changes)
                .updateTime(DateUtil.now())
                .build();
        notifierDispatcher.sendWebChangeMessage(configChangeDTO);
    }

}
