package com.zicca.zthread.core.parser;

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
