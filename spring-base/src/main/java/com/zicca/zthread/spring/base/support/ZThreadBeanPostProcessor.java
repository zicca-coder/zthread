package com.zicca.zthread.spring.base.support;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.zicca.zthread.core.config.BootstrapConfigProperties;
import com.zicca.zthread.core.executor.ThreadPoolExecutorProperties;
import com.zicca.zthread.core.executor.ZThreadExecutor;
import com.zicca.zthread.core.executor.ZThreadRegistry;
import com.zicca.zthread.core.executor.support.BlockingQueueTypeEnum;
import com.zicca.zthread.core.executor.support.RejectedPolicyTypeEnum;
import com.zicca.zthread.spring.base.DynamicThreadPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池后置处理器，扫描 Bean 是否为动态线程池，如果是的话进行属性填充和注册
 *
 * @author zicca
 */
@Slf4j
@RequiredArgsConstructor
public class ZThreadBeanPostProcessor implements BeanPostProcessor {

    private final BootstrapConfigProperties properties;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ZThreadExecutor) {
            DynamicThreadPool dynamicThreadPool;
            try {
                // 通过 IOC 容器扫描 Bean 是否存在动态线程池注解
                dynamicThreadPool = ApplicationContextHolder.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                if (Objects.isNull(dynamicThreadPool)) {
                    return bean;
                }
            } catch (Exception ex) {
                log.error("Failed to create dynamic thread pool in annotation mode.", ex);
                return bean;
            }

            ZThreadExecutor zThreadExecutor = (ZThreadExecutor) bean;
            // 从配置中心读取动态线程池配置并对本地线程池进行赋值
            ThreadPoolExecutorProperties executorProperties = properties.getExecutors()
                    .stream()
                    .filter(item -> Objects.equals(zThreadExecutor.getThreadPoolId(), item.getThreadPoolId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("The thread pool id does not exist in the configuration."));

            // 覆盖本地线程池配置
            overrideLocalThreadPoolConfig(executorProperties, zThreadExecutor);

            // 注册动态线程池到注册器，后续监控和报警从注册器获取线程池实例。同时，参数动态变更需要依赖 ThreadPoolExecutorProperties 比对是否有变更。
            ZThreadRegistry.putHolder(zThreadExecutor.getThreadPoolId(), zThreadExecutor, executorProperties);
        }

        return bean;
    }


    /**
     * 覆盖本地线程池配置
     *
     * @param executorProperties 动态线程池属性
     * @param zThreadExecutor    本地线程池实例
     */
    private void overrideLocalThreadPoolConfig(ThreadPoolExecutorProperties executorProperties, ZThreadExecutor zThreadExecutor) {
        Integer remoteCorePoolSize = executorProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = executorProperties.getMaximumPoolSize();
        Assert.isTrue(remoteCorePoolSize <= remoteMaximumPoolSize, "remoteCorePoolSize must be smaller than remoteMaximumPoolSize.");

        // 如果不清楚为什么有这段逻辑，可以参考 Hippo4j Issue https://github.com/opengoofy/hippo4j/issues/1063
        int originalMaximumPoolSize = zThreadExecutor.getMaximumPoolSize();
        if (remoteCorePoolSize > originalMaximumPoolSize) {
            zThreadExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
            zThreadExecutor.setCorePoolSize(remoteCorePoolSize);
        } else {
            zThreadExecutor.setCorePoolSize(remoteCorePoolSize);
            zThreadExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
        }

        // 阻塞队列没有常规 set 方法，所以使用反射赋值
        BlockingQueue workQueue = BlockingQueueTypeEnum.createBlockingQueue(executorProperties.getWorkQueue(), executorProperties.getQueueCapacity());
        // Java 9+ 的模块系统（JPMS）默认禁止通过反射访问 JDK 内部 API 的私有字段，所以需要配置开放反射权限
        // 在启动命令中增加以下参数，显式开放 java.util.concurrent 包
        // IDE 中通过在 VM options 中添加参数：--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
        // 部署的时候，在启动脚本（如 java -jar 命令）中加入该参数：java -jar --add-opens=java.base/java.util.concurrent=ALL-UNNAMED your-app.jar
        ReflectUtil.setFieldValue(zThreadExecutor, "workQueue", workQueue);

        // 赋值动态线程池其他核心参数
        zThreadExecutor.setKeepAliveTime(executorProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        zThreadExecutor.allowCoreThreadTimeOut(executorProperties.getAllowCoreThreadTimeOut());
        zThreadExecutor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(executorProperties.getRejectedHandler()));
    }


}
