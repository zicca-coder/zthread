package com.zicca.zthread.core.constant;

/**
 * 动态线程池基础常量类
 *
 * @author zicca
 */
public class Constants {
    /**
     * 线程池参数变更日志打印常量
     */
    public static final String CHANGE_THREAD_POOL_TEXT = "[{}] Dynamic thread pool parameter changed:"
            + "\n    corePoolSize: {}"
            + "\n    maximumPoolSize: {}"
            + "\n    capacity: {}"
            + "\n    keepAliveTime: {}"
            + "\n    rejectedType: {}"
            + "\n    allowCoreThreadTimeOut: {}";

    /**
     * 线程池参数变更前后分隔符常量
     */
    public static final String CHANGE_DELIMITER = "%s => %s";

}
