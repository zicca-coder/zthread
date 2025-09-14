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

/**
 * Web 线程池控制台修改请求实体
 *
 * @author zicca
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class WebThreadPoolUpdateReqDTO {

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
     * 核心线程数
     */
    @Min(value = 1, message = "核心线程至少为1")
    @Max(value = 1000, message = "核心线程不能超过24")
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    @Min(value = 1, message = "最大线程至少为1")
    @Max(value = 1000, message = "最大线程不能超过24")
    private Integer maximumPoolSize;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    @Min(value = 1, message = "空闲回收至少为1秒")
    @Max(value = 10000, message = "空闲回收不能超过10000秒")
    private Long keepAliveTime;

    /**
     * 通知配置
     */
    @Valid
    private NotifyConfig notify;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         */
        @Size(max = 64, message = "接收人长度不能超过64字符")
        private String receives;
    }
}
