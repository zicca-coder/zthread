package com.zicca.zthread.web.starter.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取线程池的完整运行时状态（可能设计锁操作，不建议高频调用）
 *
 * @author zicca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolState {

    /**
     * 线程池核心线程数
     */
    private Integer corePoolSize;

    /**
     * 线程池最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 线程池当前线程数
     */
    private Integer currentPoolSize;

    /**
     * 线程池活动线程数
     */
    private Integer activePoolSize;

    /**
     * 线程池最大线程数
     */
    private Integer largestPoolSize;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime;

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
     * 拒绝策略名称
     */
    private String rejectedHandlerName;

}
