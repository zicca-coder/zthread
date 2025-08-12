package com.zicca.zthread.core.parser;

import java.io.IOException;
import java.util.*;

/**
 * 配置解析器处理
 *
 * @author zicca
 */
public class ConfigParserHandler {

    private static final List<ConfigParser> PARSERS = new ArrayList<>();

    private ConfigParserHandler() {
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new PropertiesConfigParser());
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }
        return Collections.emptyMap();
    }

    public static ConfigParserHandler getInstance() {
        return ConfigParserHandlerHolder.INSTANCE;
    }

    private static class ConfigParserHandlerHolder {

        private static final ConfigParserHandler INSTANCE = new ConfigParserHandler();
    }

}
