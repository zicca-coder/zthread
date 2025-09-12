package com.zicca.zthread.web.starter.core.executor;

import com.zicca.zthread.spring.base.support.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;

/**
 * @author zicca
 */
@Slf4j
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

        // 检查是否为 WebServerApplicationContext 类型
        if (applicationContext instanceof WebServerApplicationContext) {
            return ((WebServerApplicationContext) applicationContext).getWebServer();
        }

        // 在测试环境或其他非web环境中返回 null
        log.warn("ApplicationContext is not a WebServerApplicationContext. Current type: {}", applicationContext.getClass().getName());
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            WebServer webServer = getWebServer();
            if (webServer != null) {
                Executor webExecutor = getExecutor(webServer);
                executor = webExecutor;
            } else {
                log.info("WebServer not avaliable, skipping web thread pool initialization.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize web thread pool", e);
            throw e;
        }
    }

}
