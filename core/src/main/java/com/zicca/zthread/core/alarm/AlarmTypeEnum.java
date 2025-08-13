package com.zicca.zthread.core.alarm;

import lombok.Getter;

import java.util.Arrays;

/**
 * 告警类型枚举
 */
public enum AlarmTypeEnum {

    ACTIVITY("Activity", "线程池线程活跃度告警"),
    CAPACITY("Capacity", "线程池队列容量告警"),
    REJECTED("Rejected", "线程池拒绝策略告警"),
    ;
    @Getter
    private final String value;
    @Getter
    private final String desc;

    AlarmTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static AlarmTypeEnum of(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equals(value))
                .findFirst().orElseThrow(() -> new RuntimeException("The alarm type does not exist."));
    }

}
