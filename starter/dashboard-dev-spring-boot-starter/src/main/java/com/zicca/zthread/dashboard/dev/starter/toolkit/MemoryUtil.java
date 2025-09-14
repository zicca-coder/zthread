package com.zicca.zthread.dashboard.dev.starter.toolkit;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * 内存工具类
 *
 * @author zicca
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemoryUtil {

    /**
     * 内存使用对象
     */
    private static MemoryUsage HEAP_MEMORY_USAGE = ManagementFactory
            .getMemoryMXBean()
            .getHeapMemoryUsage();


    /**
     * 获取JVM堆内存已使用大小
     *
     * @return 返回堆内存已使用的字节数
     */
    public static long heapMemoryUsed() {
        return HEAP_MEMORY_USAGE.getUsed();
    }

    /**
     * 获取堆内存最大使用量
     *
     * @return 堆内存最大使用量，单位为字节
     */
    public static long heapMemoryMax() {
        return HEAP_MEMORY_USAGE.getMax();
    }

    /**
     * 获取系统可用内存大小
     *
     * @return 返回格式化后的可用内存大小字符串，单位可能为B、KB、MB、GB等
     */
    public static String getFreeMemory() {
        // 计算可用内存：最大堆内存减去已使用堆内存，然后转换为可读格式
        return ByteConvertUtil.getPrintSize(Math.subtractExact(heapMemoryMax(), heapMemoryUsed()));
    }

    /**
     * 获取内存使用比例的格式化字符串
     *
     * @return 格式化后的内存使用比例字符串，格式为"Allocation: {已使用内存} / Maximum available: {最大可用内存}"
     */
    public static String getMemoryProportion() {
        return StrUtil.format(
                "Allocation: {} / Maximum available: {}",
                ByteConvertUtil.getPrintSize(heapMemoryUsed()),
                ByteConvertUtil.getPrintSize(heapMemoryMax())
        );
    }

}
