package com.zicca.zthread.nacos.cloud.example;

import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池性能测试
 *
 * @author zicca
 */
@SpringBootTest
public class ThreadPoolPerformanceTest {


    @Autowired
    @Qualifier("zthreadPoolExecutor")
    private ThreadPoolExecutor dynamicThreadPool;

    @Autowired
    @Qualifier("threadPoolExecutor")
    private ThreadPoolExecutor jdkThreadPool;

    /**
     * 测试线程池执行任务的吞吐量
     */
    @Test
    public void testThroughput() throws InterruptedException {
        int taskCount = 10000;
        CountDownLatch dynamicLatch = new CountDownLatch(taskCount);
        CountDownLatch jdkLatch = new CountDownLatch(taskCount);

        // 测试动态线程池吞吐量
        long dynamicStartTime = System.currentTimeMillis();
        for (int i = 0; i < taskCount; i++) {
            dynamicThreadPool.execute(() -> {
                // 模拟简单任务
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                dynamicLatch.countDown();
            });
        }
        dynamicLatch.await();
        long dynamicEndTime = System.currentTimeMillis();

        // 测试JDK线程池吞吐量
        long jdkStartTime = System.currentTimeMillis();
        for (int i = 0; i < taskCount; i++) {
            jdkThreadPool.execute(() -> {
                // 模拟相同任务
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jdkLatch.countDown();
            });
        }
        jdkLatch.await();
        long jdkEndTime = System.currentTimeMillis();

        System.out.println("Dynamic ThreadPool execution time: " + (dynamicEndTime - dynamicStartTime) + "ms");
        System.out.println("JDK ThreadPool execution time: " + (jdkEndTime - jdkStartTime) + "ms");
    }

    @Test
    public void testConcurrentProcessing() throws InterruptedException {
        int concurrentTasks = 100;
        int iterations = 100;

        CountDownLatch dynamicLatch = new CountDownLatch(concurrentTasks * iterations);
        CountDownLatch jdkLatch = new CountDownLatch(concurrentTasks * iterations);

        AtomicLong dynamicTotalTime = new AtomicLong(0);
        AtomicLong jdkTotalTime = new AtomicLong(0);

        // 动态线程池并发测试
        long dynamicStartTime = System.currentTimeMillis();
        for (int i = 0; i < concurrentTasks; i++) {
            dynamicThreadPool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    long taskStart = System.nanoTime();
                    // 模拟CPU密集型任务
                    performCalculation();
                    long taskEnd = System.nanoTime();
                    dynamicTotalTime.addAndGet(taskEnd - taskStart);
                    dynamicLatch.countDown();
                }
            });
        }
        dynamicLatch.await();
        long dynamicEndTime = System.currentTimeMillis();

        // JDK线程池并发测试
        long jdkStartTime = System.currentTimeMillis();
        for (int i = 0; i < concurrentTasks; i++) {
            jdkThreadPool.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    long taskStart = System.nanoTime();
                    // 模拟相同CPU密集型任务
                    performCalculation();
                    long taskEnd = System.nanoTime();
                    jdkTotalTime.addAndGet(taskEnd - taskStart);
                    jdkLatch.countDown();
                }
            });
        }
        jdkLatch.await();
        long jdkEndTime = System.currentTimeMillis();

        System.out.println("Dynamic ThreadPool concurrent processing time: " + (dynamicEndTime - dynamicStartTime) + "ms");
        System.out.println("JDK ThreadPool concurrent processing time: " + (jdkEndTime - jdkStartTime) + "ms");
        System.out.println("Dynamic ThreadPool average task time: " + (dynamicTotalTime.get() / (concurrentTasks * iterations) / 1000) + "μs");
        System.out.println("JDK ThreadPool average task time: " + (jdkTotalTime.get() / (concurrentTasks * iterations) / 1000) + "μs");
    }

    private void performCalculation() {
        // 模拟CPU计算任务
        double result = 0;
        for (int i = 0; i < 1000; i++) {
            result += Math.sqrt(i) * Math.sin(i);
        }
    }


    @Test
    public void testMemoryUsage() {
        // 监控线程池内存使用情况
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // 测试前内存状态
        MemoryUsage beforeDynamic = memoryBean.getHeapMemoryUsage();

        // 提交大量任务到动态线程池
        submitTasks(dynamicThreadPool, 5000);

        // 强制GC后检查内存
        System.gc();
        MemoryUsage afterDynamic = memoryBean.getHeapMemoryUsage();

        // 重新测试JDK线程池
        System.gc();
        MemoryUsage beforeJdk = memoryBean.getHeapMemoryUsage();

        submitTasks(jdkThreadPool, 5000);

        System.gc();
        MemoryUsage afterJdk = memoryBean.getHeapMemoryUsage();

        System.out.println("Dynamic ThreadPool memory usage: " +
                (afterDynamic.getUsed() - beforeDynamic.getUsed()) / 1024 / 1024 + "MB");
        System.out.println("JDK ThreadPool memory usage: " +
                (afterJdk.getUsed() - beforeJdk.getUsed()) / 1024 / 1024 + "MB");
    }

    private void submitTasks(ThreadPoolExecutor executor, int taskCount) {
        CountDownLatch latch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testThreadPoolMetrics() throws InterruptedException {
        int taskCount = 1000;
        CountDownLatch latch = new CountDownLatch(taskCount);

        // 监控动态线程池指标
        long dynamicCompletedTasksBefore = dynamicThreadPool.getCompletedTaskCount();
        int dynamicPoolSizeBefore = dynamicThreadPool.getPoolSize();
        int dynamicActiveCountBefore = dynamicThreadPool.getActiveCount();

        // 提交任务
        for (int i = 0; i < taskCount; i++) {
            CountDownLatch finalLatch = latch;
            dynamicThreadPool.submit(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finalLatch.countDown();
            });
        }

        // 等待任务完成
        latch.await(60, TimeUnit.SECONDS);

        long dynamicCompletedTasksAfter = dynamicThreadPool.getCompletedTaskCount();
        int dynamicPoolSizeAfter = dynamicThreadPool.getPoolSize();
        int dynamicActiveCountAfter = dynamicThreadPool.getActiveCount();

        System.out.println("=== Dynamic ThreadPool Metrics ===");
        System.out.println("Completed tasks: " + (dynamicCompletedTasksAfter - dynamicCompletedTasksBefore));
        System.out.println("Pool size change: " + dynamicPoolSizeBefore + " -> " + dynamicPoolSizeAfter);
        System.out.println("Active count change: " + dynamicActiveCountBefore + " -> " + dynamicActiveCountAfter);

        // 重置并测试JDK线程池
        latch = new CountDownLatch(taskCount);
        long jdkCompletedTasksBefore = jdkThreadPool.getCompletedTaskCount();
        int jdkPoolSizeBefore = jdkThreadPool.getPoolSize();
        int jdkActiveCountBefore = jdkThreadPool.getActiveCount();

        for (int i = 0; i < taskCount; i++) {
            CountDownLatch finalLatch1 = latch;
            jdkThreadPool.submit(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finalLatch1.countDown();
            });
        }

        latch.await(60, TimeUnit.SECONDS);

        long jdkCompletedTasksAfter = jdkThreadPool.getCompletedTaskCount();
        int jdkPoolSizeAfter = jdkThreadPool.getPoolSize();
        int jdkActiveCountAfter = jdkThreadPool.getActiveCount();

        System.out.println("=== JDK ThreadPool Metrics ===");
        System.out.println("Completed tasks: " + (jdkCompletedTasksAfter - jdkCompletedTasksBefore));
        System.out.println("Pool size change: " + jdkPoolSizeBefore + " -> " + jdkPoolSizeAfter);
        System.out.println("Active count change: " + jdkActiveCountBefore + " -> " + jdkActiveCountAfter);
    }

    @Test
    public void testHighLoadPerformance() throws InterruptedException {
        int taskCount = 50000;
        CountDownLatch dynamicLatch = new CountDownLatch(taskCount);
        CountDownLatch jdkLatch = new CountDownLatch(taskCount);

        // 记录系统负载
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        // 动态线程池高负载测试
        double cpuBeforeDynamic = osBean.getSystemCpuLoad();
        long dynamicStartTime = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            dynamicThreadPool.submit(() -> {
                // 模拟实际业务任务
                processBusinessLogic();
                dynamicLatch.countDown();
            });
        }

        dynamicLatch.await();
        long dynamicEndTime = System.currentTimeMillis();
        double cpuAfterDynamic = osBean.getSystemCpuLoad();

        // JDK线程池高负载测试
        double cpuBeforeJdk = osBean.getSystemCpuLoad();
        long jdkStartTime = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            jdkThreadPool.submit(() -> {
                // 模拟相同业务任务
                processBusinessLogic();
                jdkLatch.countDown();
            });
        }

        jdkLatch.await();
        long jdkEndTime = System.currentTimeMillis();
        double cpuAfterJdk = osBean.getSystemCpuLoad();

        System.out.println("=== High Load Performance Test ===");
        System.out.println("Dynamic ThreadPool total time: " + (dynamicEndTime - dynamicStartTime) + "ms");
        System.out.println("Dynamic ThreadPool throughput: " + (taskCount * 1000.0 / (dynamicEndTime - dynamicStartTime)) + " tasks/sec");
        System.out.println("Dynamic ThreadPool CPU usage change: " + (cpuAfterDynamic - cpuBeforeDynamic));

        System.out.println("JDK ThreadPool total time: " + (jdkEndTime - jdkStartTime) + "ms");
        System.out.println("JDK ThreadPool throughput: " + (taskCount * 1000.0 / (jdkEndTime - jdkStartTime)) + " tasks/sec");
        System.out.println("JDK ThreadPool CPU usage change: " + (cpuAfterJdk - cpuBeforeJdk));
    }

    private void processBusinessLogic() {
        // 模拟实际业务处理逻辑
        try {
            // 模拟IO操作
            Thread.sleep(1);

            // 模拟计算操作
            int result = 0;
            for (int i = 0; i < 100; i++) {
                result += i * i;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testDynamicConfigurationChange() throws InterruptedException {
        // 测试动态线程池对配置变更的响应能力
        int initialTaskCount = 1000;
        CountDownLatch initialLatch = new CountDownLatch(initialTaskCount);

        // 初始状态下的性能
        long initialStartTime = System.currentTimeMillis();
        for (int i = 0; i < initialTaskCount; i++) {
            dynamicThreadPool.submit(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                initialLatch.countDown();
            });
        }
        initialLatch.await();
        long initialEndTime = System.currentTimeMillis();

        System.out.println("Initial performance with dynamic thread pool: " + (initialEndTime - initialStartTime) + "ms");

        // 这里可以模拟配置变更，例如通过Nacos修改配置
        // 然后再次测试性能表现
    }

}
