package com.zicca.zthread.core.executor.support;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拒绝策略代理处理器
 *
 * <p>
 * 用于通过 JDK 动态代理包装 {@link RejectedExecutionHandler}，统计线程池被拒绝的次数
 * 当调用的是 {@code rejectedExecution} 方法时进行计数
 * </p>
 *
 * <p>
 * 示例用途：用于线程池拒绝报警、拒绝率分析等运行时动态监控
 * </p>
 *
 * @author zicca
 */
@AllArgsConstructor
public class RejectedProxyInvocationHandler implements InvocationHandler {

    private final Object target;
    private final AtomicLong rejectCount;

    private static final String REJEXT_METHOD = "rejectedExecution";

    /**
     * 通过动态代理来拦截对目标对象的方法调用，添加额外的监控功能（拒绝任务计数），同时保持原有功能不变
     *
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 当调用的方法名为 rejectedExecution 时
        // 检查参数是否符合要求：有两个参数，分别是 Runnable 和 ThreadPoolExecutor 类型
        // 如果符合条件，使用 AtomicLong 进行计数
        if (REJEXT_METHOD.equals(method.getName()) &&
                args != null &&
                args.length == 2 &&
                args[0] instanceof Runnable &&
                args[1] instanceof ThreadPoolExecutor) {
            rejectCount.incrementAndGet();
        }

        // 当调用无参的 toString 方法时，返回类名
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return target.getClass().getSimpleName();
        }

        // 否则，调用目标对象的方法
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
