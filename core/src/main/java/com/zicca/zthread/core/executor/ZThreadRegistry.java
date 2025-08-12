package com.zicca.zthread.core.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池管理器，用于统一管理线程池实例
 *
 * @author zicca
 */
public class ZThreadRegistry {

    /**
     * 线程池持有者缓存，key为线程池唯一标识，value为线程池包装类
     */
    private static final Map<String, ThreadPoolExecutorHolder> HOLDER_MAP = new ConcurrentHashMap<>();

    /**
     * 注册线程池到管理器
     *
     * @param threadPoolId 线程池唯一标识
     * @param executor     线程池实例
     * @param properties   线程池参数配置
     */
    public static void putHolder(String threadPoolId, ThreadPoolExecutor executor, ThreadPoolExecutorProperties properties) {
        ThreadPoolExecutorHolder executorHolder = new ThreadPoolExecutorHolder(threadPoolId, executor, properties);
        HOLDER_MAP.put(threadPoolId, executorHolder);
    }

    /**
     * 获取线程池包装类
     *
     * @param threadPoolId 线程池唯一标识
     * @return 线程池包装类
     */
    public static ThreadPoolExecutorHolder getHolder(String threadPoolId) {
        return HOLDER_MAP.get(threadPoolId);
    }

    /**
     * 获取所有线程池包装类
     *
     * @return 所有线程池包装类
     */
    public static Collection<ThreadPoolExecutorHolder> getAllHolders() {
        return HOLDER_MAP.values();
    }

}
