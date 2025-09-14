package com.zicca.zthread.spring.base.support;

import com.zicca.zthread.core.config.ApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * 动态线程池 Spring 配置加载
 *
 * @author zicca
 */
public class SpringPropertiesLoader implements InitializingBean {

    @Value("${spring.application.name:UNKNOWN}")
    private String applicationName;

    @Value("${spring.profiles.active:UNKNOWN}")
    private String activeProfile;


    @Override
    public void afterPropertiesSet() throws Exception {
        ApplicationProperties.setApplicationName(applicationName);
        ApplicationProperties.setActiveProfile(activeProfile);
    }
}
