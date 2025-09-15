package com.zicca.zthread.web.starter.configuration;

import com.zicca.zthread.core.notification.service.NotifierDispatcher;
import com.zicca.zthread.web.starter.core.WebThreadPoolRefreshListener;
import com.zicca.zthread.web.starter.core.executor.JettyWebThreadPoolService;
import com.zicca.zthread.web.starter.core.executor.TomcatWebThreadPoolService;
import com.zicca.zthread.web.starter.core.executor.WebThreadPoolService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 容器动态线程池适配自动装配配置类
 *
 * @author zicca
 */
@Configuration
public class WebAdapterConfiguration {


    @Bean(initMethod = "init")
    @ConditionalOnClass(name = {"org.apache.catalina.startup.Tomcat", "org.apache.coyote.UpgradeProtocol", "jakarta.servlet.Servlet"})
    @ConditionalOnBean(value = ConfigurableTomcatWebServerFactory.class, search = SearchStrategy.CURRENT)
    public TomcatWebThreadPoolService tomcatWebThreadPoolService() {
        return new TomcatWebThreadPoolService();
    }

    @Bean("init")
    @ConditionalOnClass(
            name = {
                    "jakarta.servlet.Servlet", "org.eclipse.jetty.server.Server",
                    "org.eclipse.jetty.util.Loader", "org.eclipse.jetty.ee10.webapp.WebAppContext"
            })
    @ConditionalOnBean(value = ConfigurableJettyWebServerFactory.class, search = SearchStrategy.CURRENT)
    public JettyWebThreadPoolService jettyWebThreadPoolService() {
        return new JettyWebThreadPoolService();
    }

    @Bean
    public WebThreadPoolRefreshListener webThreadPoolRefreshListener(@SuppressWarnings("all")
                                                                     WebThreadPoolService webThreadPoolService,
                                                                     NotifierDispatcher notifierDispatcher) {
        return new WebThreadPoolRefreshListener(webThreadPoolService, notifierDispatcher);
    }


}
