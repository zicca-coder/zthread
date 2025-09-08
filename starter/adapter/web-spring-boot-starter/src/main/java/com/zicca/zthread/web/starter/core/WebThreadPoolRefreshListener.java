package com.zicca.zthread.web.starter.core;

import com.zicca.zthread.config.common.starter.refresher.ThreadPoolConfigUpdateEvent;
import org.springframework.context.ApplicationListener;

/**
 * Web 线程池监听配置中心刷新事件
 *
 * @author zicca
 */
public class WebThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {
    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {

    }
}
