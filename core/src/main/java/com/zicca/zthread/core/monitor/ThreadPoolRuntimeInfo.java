package com.zicca.zthread.core.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线程池运行时监控实体
 *
 * @author zicca
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolRuntimeInfo {

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 当前线程数
     */
    private Integer currentPoolSize;

    /**
     * 活跃线程数
     */
    private Integer activePoolSize;

    /**
     * 最大线程数
     */
    private Integer largestPoolSize;

    /**
     * 线程池任务总量
     */
    private Long completedTaskCount;

    /**
     * 阻塞队列类型
     */
    private String workQueueName;

    /**
     * 队列容量
     */
    private Integer workQueueCapacity;

    /**
     * 队列元素数量
     */
    private Integer workQueueSize;

    /**
     * 队列剩余容量
     */
    private Integer workQueueRemainingCapacity;

    /**
     * 拒绝策略
     */
    private String rejectedHandlerName;

    /**
     * 执行拒绝策略次数
     */
    private Long rejectCount;


}
