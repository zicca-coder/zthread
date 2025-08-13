package com.zicca.zthread.core.notification.service;

import com.zicca.zthread.core.notification.dto.ThreadPoolAlarmNotifyDTO;

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
}
