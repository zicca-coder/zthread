package com.zicca.zthread.core.parser;

import lombok.Getter;

import java.util.Arrays;

/**
 * 配置文件类型枚举
 *
 * @author zicca
 */
@Getter
public enum ConfigFileTypeEnum {

    /**
     *  properties
     */
    PROPERTIES("properties"),

    /**
     * yaml
     */
    YAML("yaml"),

    /**
     *  yml
     */
    YML("xml"),
    ;

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst().orElse(PROPERTIES);
    }
}
