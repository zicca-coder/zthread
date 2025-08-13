package com.zicca.zthread.core.notification.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池告警速率限流器
 *
 * @author zicca
 */
public class AlarmRateLimiter {

    /**
     * 报警记录缓存 key: threadPoolId + "|" + alarmType
     * value: 上次发送告警的时间戳
     */
    private static final Map<String, Long> ALARM_RECORD = new ConcurrentHashMap<>();

    /**
     * 定期清理过期记录
     */
    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();

    static {
        // 每小时清理一次超过24小时的记录
        CLEANER.scheduleAtFixedRate(() -> {
            long expiredTime = System.currentTimeMillis() - 24 * 60 * 1000;
            ALARM_RECORD.entrySet().removeIf(entry -> entry.getValue() < expiredTime);
        }, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 构建缓存键
     */
    private static String buildKey(String threadPoolId, String alarmType) {
        return threadPoolId + "|" + alarmType;
    }

    /**
     * 检查是否允许发送警报
     *
     * @param threadPoolId    线程池 ID
     * @param alarmType       报警类型
     * @param intervalMinutes 间隔分钟数
     * @return true表示允许发送，false表示不允许发送
     */
    public static boolean allowAlarm(String threadPoolId, String alarmType, int intervalMinutes) {
        String key = buildKey(threadPoolId, alarmType);
        long currentTime = System.currentTimeMillis();
        long intervalMillis = intervalMinutes * 60 * 1000L;

        return ALARM_RECORD.compute(key, (k, lastTime) -> {
            if (lastTime == null || (currentTime - lastTime) > intervalMillis) {
                return currentTime; // 更新为当前时间
            }
            return lastTime; // 保持原时间
        }) == currentTime; // 返回值等于当前时间说明允许发送
    }


}
