package com.zicca.zthread.dashboard.dev.server.service.handler;

/**
 * 抽象配置解析器
 *
 * @author zicca
 */
public abstract class AbstractConfigParser implements ConfigParser{
    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return getConfigFileTypes().contains(type);
    }
}
