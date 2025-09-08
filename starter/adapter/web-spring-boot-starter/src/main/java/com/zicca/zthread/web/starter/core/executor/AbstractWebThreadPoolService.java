package com.zicca.zthread.web.starter.core.executor;

import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;

/**
 * @author zicca
 */
public abstract class AbstractWebThreadPoolService implements WebThreadPoolService, ApplicationRunner {

    protected Executor executor;

    /**
     * 获取当前 Web 容器的线程池执行器
     *
     * @param webServer Web 服务器，例如 Tomcat、Jetty、Undertow
     * @return 当前 Web 容器的 Executor 实例
     */
    protected abstract Executor getExecutor(WebServer webServer);

    @Override
    public String getRunningStatus() {
        // 可扩展
        return "Running";
    }

    /**
     * 获取当前应用的 WebServer 实例
     * <p>
     * 通过 ApplicationContextHolder 获取 Spring 的 ApplicationContext，
     * 并将其转换为 WebServerApplicationContext 以获取 WebServer。
     * </p>
     *
     * @return 当前应用的 WebServer 实例
     */
    public WebServer getWebServer() {
        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        return ((WebServerApplicationContext) applicationContext).getWebServer();
    }

    @Override
    public void run(ApplicationArguments args) {
        Executor webExecutor = getExecutor(getWebServer());
        executor = webExecutor;
    }

}
