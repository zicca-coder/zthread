package com.zicca.zthread.dashboard.dev.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**线程池控制台修改请求实体
 * @author zicca
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolUpdateReqDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 数据 ID
     */
    private String dataId;

    /**
     * 分组标识
     */
    private String group;

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 核心线程数
     */
    @Min(value = 1, message = "核心线程至少为1")
    @Max(value = 24, message = "核心线程不能超过24")
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    @Min(value = 1, message = "最大线程至少为1")
    @Max(value = 24, message = "最大线程不能超过24")
    private Integer maximumPoolSize;

    /**
     * 队列容量
     */
    @Min(value = 0, message = "队列容量至少为1")
    @Max(value = 99999999, message = "队列容量不能超过99999999")
    private Integer queueCapacity;

    /**
     * 阻塞队列类型
     */
    private String workQueue;

    /**
     * 拒绝策略类型
     */
    private String rejectedHandler;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    @Min(value = 1, message = "空闲回收至少为1秒")
    @Max(value = 10000, message = "空闲回收不能超过10000秒")
    private Long keepAliveTime;

    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * 通知配置
     */
    @Valid
    private NotifyConfig notify;

    /**
     * 报警配置，默认设置
     */
    @Valid
    @Builder.Default
    private AlarmConfig alarm = new AlarmConfig();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         */
        @Size(max = 64, message = "接收人长度不能超过64字符")
        private String receives;

        /**
         * 告警间隔，单位分钟
         */
        @Min(value = 1, message = "告警间隔至少为1分钟")
        @Max(value = 30, message = "告警间隔不能超过30分钟")
        private Integer interval = 5;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmConfig {

        /**
         * 默认开启报警配配置
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 队列阈值
         */
        @Min(value = 0, message = "容量告警设置过小")
        @Max(value = 100, message = "容量告警设置过大")
        private Integer queueThreshold = 80;

        /**
         * 活跃线程阈值
         */
        @Min(value = 0, message = "活跃告警设置过小")
        @Max(value = 100, message = "活跃告警设置过大")
        private Integer activeThreshold = 80;
    }
}
