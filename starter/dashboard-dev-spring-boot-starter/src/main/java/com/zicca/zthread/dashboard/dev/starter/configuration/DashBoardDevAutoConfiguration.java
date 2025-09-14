package com.zicca.zthread.dashboard.dev.starter.configuration;

import com.zicca.zthread.dashboard.dev.starter.controller.DynamicThreadPoolController;
import com.zicca.zthread.dashboard.dev.starter.controller.WebThreadPoolController;
import com.zicca.zthread.dashboard.dev.starter.service.IDynamicThreadPoolService;
import com.zicca.zthread.dashboard.dev.starter.service.IWebThreadPoolService;
import com.zicca.zthread.web.starter.core.executor.WebThreadPoolService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于配置中心的公共自动装配配置
 *
 * @author zicca
 */
@Configuration
public class DashBoardDevAutoConfiguration {

    @Bean
    public IDynamicThreadPoolService iDynamicThreadPoolService() {
        return new IDynamicThreadPoolService();
    }

    @Bean
    public IWebThreadPoolService iWebThreadPoolService(WebThreadPoolService webThreadPoolService) {
        return new IWebThreadPoolService(webThreadPoolService);
    }

    @Bean
    public DynamicThreadPoolController dynamicThreadPoolController(IDynamicThreadPoolService iDynamicThreadPoolService) {
        return new DynamicThreadPoolController(iDynamicThreadPoolService);
    }

    @Bean
    public WebThreadPoolController webThreadPoolController(IWebThreadPoolService iWebThreadPoolService) {
        return new WebThreadPoolController(iWebThreadPoolService);
    }


}
