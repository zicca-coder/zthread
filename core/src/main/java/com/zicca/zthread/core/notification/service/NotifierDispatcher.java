package com.zicca.zthread.core.notification.service;

import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.notification.dto.ThreadPoolAlarmNotifyDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知调度器，用于统一管理和路由各类通知发送器（如钉钉、飞书、企业微信等）
 * <p>
 * 该类屏蔽了具体通知平台的实现细节，对上层调用者提供统一的通知发送入口
 * 内部根据配置自动初始化可用的 Notifier 实现，并在发送通知时根据平台类型动态路由到对应的发送器
 * <p>
 * @author zicca
 */
public class NotifierDispatcher implements NotifierService{

    private static final Map<String, NotifierService> NOTIFIER_SERVICE_MAP = new HashMap<>();

    static {
        // 在简单工厂中注册不同的通知实现
        NOTIFIER_SERVICE_MAP.put("DING", new DingTalkMessageService());
//        后续扩展实现
//        NOTIFIER_SERVICE_MAP.put("FEISHU", new FeiShuMessageService());
//        NOTIFIER_SERVICE_MAP.put("WX", new WXMessageService());
//        NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
    }



    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        getNotifierService().ifPresent(service -> {
            service.sendAlarmMessage(alarm);
        });
    }


    /**
     * 根据配置获取对应的通知服务实现
     *
     * @return 通知服务实现
     */
    private Optional<NotifierService> getNotifierService() {
        return Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(platform -> NOTIFIER_SERVICE_MAP.get(platform));
    }

}
