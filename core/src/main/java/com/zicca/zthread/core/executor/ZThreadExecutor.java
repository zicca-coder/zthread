package com.zicca.zthread.core.executor;


import com.zicca.zthread.core.executor.support.RejectedProxyInvocationHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强的动态、报警和受监控的线程池 ZThreadExecutor
 *
 * @author zicca
 */
@Slf4j
public class ZThreadExecutor extends ThreadPoolExecutor {

    /**
     * 线程池唯一标识，用来动态变更参数等
     */
    @Getter
    private final String threadPoolId;

    /**
     * 线程池拒绝策略执行次数
     */
    @Getter
    private final AtomicLong rejectCount = new AtomicLong();

    /**
     * 等待终止时间，单位毫秒
     */
    private long awaitTerminationMillis;

    /**
     * Creates a new {@code ExtensibleThreadPoolExecutor} with the given initial parameters.
     *
     * @param threadPoolId           thread-pool id
     * @param corePoolSize           the number of threads to keep in the pool, even
     *                               if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize        the maximum number of threads to allow in the
     *                               pool
     * @param keepAliveTime          when the number of threads is greater than
     *                               the core, this is the maximum time that excess idle threads
     *                               will wait for new tasks before terminating.
     * @param unit                   the time unit for the {@code keepAliveTime} argument
     * @param workQueue              the queue to use for holding tasks before they are
     *                               executed.  This queue will hold only the {@code Runnable}
     *                               tasks submitted by the {@code execute} method.
     * @param threadFactory          the factory to use when the executor
     *                               creates a new thread
     * @param handler                the handler to use when execution is blocked
     *                               because the thread bounds and queue capacities are reached
     * @param awaitTerminationMillis the maximum time to wait
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     if {@code workQueue} or {@code unit}
     *                                  or {@code threadFactory} or {@code handler} is null
     */
    public ZThreadExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        // 通过动态代理设置拒绝策略执行次数
        setRejectedExecutionHandler(handler);

        // 设置线程池扩展属性：线程池 ID 标识
        this.threadPoolId = threadPoolId;

        // 设置等待终止时间，单位毫秒
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        // 创建动态代理
        // 使用 Proxy.newProxyInstance() 创建 RejectedExecutionHandler 的动态代理实例
        // 代理对象基于传入的原始拒绝策略处理器 handler
        // 使用 RejectedProxyInvocationHandler 最为调用处理器，传入 rejectCount 计数器
        RejectedExecutionHandler rejectedProxy = (RejectedExecutionHandler) Proxy
                .newProxyInstance(
                        handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedProxyInvocationHandler(handler, rejectCount)
                );
        // 设置代理处理器
        // 调用父类的 setRejectedExecutionHandler() 方法，将代理处理器设置为线程池的拒绝策略处理器
        super.setRejectedExecutionHandler(rejectedProxy);
    }

    @Override
    public void shutdown() {
        // 首先检查线程池是否已经关闭
        if (isShutdown()) {
            // 如果已经关闭则直接返回，避免重复操作
            return;
        }

        // 调用父类的 shutdown() 方法，停止接收新任务
        super.shutdown();

        log.info("Before shutting down ExecutorService {}", threadPoolId);
        try {
            // 使用awaitTermination() 方法等待线程池终止，并设置超时时间为 awaitTerminationMillis，让已提交的任务执行完毕
            boolean isTerminated = this.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS);
            if (!isTerminated) {
                // 如果超时未终止，记录 warn 级别日志
                log.warn("Timed out while waiting for executor {} to terminate.", threadPoolId);
            } else {
                // 如果在指定时间内成功终止，记录 info 级别日志
                log.info("ExecutorService {} has been shutdown.", threadPoolId);
            }
        } catch (InterruptedException ex) {
            // 捕获中断异常，记录警告日志并重新设置线程的中断状态
            log.warn("Interrupted while waiting for executor {} to terminate.", threadPoolId);
            Thread.currentThread().interrupt();
        }
    }



}
