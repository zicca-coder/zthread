package com.zicca.zthread.core.notification.service;

import com.zicca.zthread.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.zicca.zthread.core.notification.dto.ThreadPoolConfigChangeDTO;
import com.zicca.zthread.core.notification.dto.WebThreadPoolConfigChangeDTO;

/**
 * 通知接口，用于发送线程池变更通知与运行时告警
 *
 * @author zicca
 */
public interface NotifierService {


    /**
     * 发送线程池报警通知
     *
     * @param alarm 报警信息
     */
    void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm);

    /**
     * 发送线程池配置变更通知
     *
     * @param configChange 配置变更信息
     */
    void sendChangeMessage(ThreadPoolConfigChangeDTO configChange);

    /**
     * 发送 Web 线程池配置变更通知
     *
     * @param configChange 配置变更信息
     */
    void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange);

}
