package com.zicca.zthread.core.notification.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.zicca.zthread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.zicca.zthread.core.notification.dto.WebThreadPoolConfigChangeDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zicca.zthread.core.constant.Constants.*;

/**
 * 顶顶消息通知服务
 *
 * @author zicca
 */
@Slf4j
public class DingTalkMessageService implements NotifierService {

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        String text = String.format(
                DING_ALARM_NOTIFY_MESSAGE_TEXT,
                alarm.getActiveProfile().toUpperCase(),
                alarm.getThreadPoolId(),
                alarm.getIdentify() + ":" + alarm.getApplicationName(),
                alarm.getAlarmType(),
                alarm.getCorePoolSize(),
                alarm.getMaximumPoolSize(),
                alarm.getCurrentPoolSize(),
                alarm.getActivePoolSize(),
                alarm.getLargestPoolSize(),
                alarm.getCompletedTaskCount(),
                alarm.getWorkQueueName(),
                alarm.getWorkQueueCapacity(),
                alarm.getWorkQueueSize(),
                alarm.getWorkQueueRemainingCapacity(),
                alarm.getRejectedHandlerName(),
                alarm.getRejectCount(),
                alarm.getReceives(),
                alarm.getInterval(),
                alarm.getCurrentTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(alarm.getReceives().split(","));
        sendDingTalkMarkdownMessage("线程池告警通知", text, atMobiles);
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChangeDTO) {
        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChangeDTO.getChanges();
        String text = String.format(
                DING_CONFIG_CHANGE_MESSAGE_TEXT,
                configChangeDTO.getActiveProfile().toUpperCase(),
                configChangeDTO.getThreadPoolId(),
                configChangeDTO.getIdentify() + ":" + configChangeDTO.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChangeDTO.getWorkQueue(),
                changes.get("queueCapacity").getBefore() + " ➲ " + changes.get("queueCapacity").getAfter(),
                changes.get("rejectedHandler").getBefore(),
                changes.get("rejectedHandler").getAfter(),
                configChangeDTO.getReceives(),
                configChangeDTO.getUpdateTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        sendDingTalkMarkdownMessage("动态线程池通知", text, atMobiles);
    }

    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChangeDTO) {
        Map<String, WebThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChangeDTO.getChanges();
        String webContainerName = configChangeDTO.getWebContainerName();
        String text = String.format(
                DING_CONFIG_WEB_CHANGE_MESSAGE_TEXT,
                configChangeDTO.getActiveProfile().toUpperCase(),
                webContainerName,
                configChangeDTO.getIdentify() + ":" + configChangeDTO.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChangeDTO.getReceives(),
                webContainerName,
                configChangeDTO.getUpdateTime()
        );

        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        sendDingTalkMarkdownMessage(webContainerName + "线程池通知", text, atMobiles);
    }

    /**
     * 通用的钉钉markdown格式发送逻辑
     *
     * @param title     标题
     * @param text      内容
     * @param atMobiles @人
     */
    private void sendDingTalkMarkdownMessage(String title, String text, List<String> atMobiles) {
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);

        Map<String, Object> request = new HashMap<>();
        request.put("msgtype", "markdown");
        request.put("markdown", markdown);
        request.put("at", at);

        try {
            String serverUrl = BootstrapConfigProperties.getInstance().getNotifyPlatforms().getUrl();
            String responseBody = HttpUtil.post(serverUrl, JSON.toJSONString(request));
            DingRobotResponse response = JSON.parseObject(responseBody, DingRobotResponse.class);
            if (response.getErrcode() != 0) {
                log.error("Ding failed to send message, reason: {}", response.errmsg);
            }
        } catch (Exception ex) {
            log.error("Ding failed to send message.", ex);
        }
    }

    @Data
    static class DingRobotResponse {
        private Long errcode;
        private String errmsg;
    }

}
