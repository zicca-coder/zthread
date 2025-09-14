package com.zicca.zthread.dashboard.dev.starter.core;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用返回结果封装类
 *
 * @author zicca
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -5324810067893002341L;

    /**
     * 成功状态码，通常使用“0”表示成功
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 失败状态码，通常使用“1”表示失败
     */
    public static final String ERROR_CODE = "1";

    /**
     * 状态码，“0”表示成功，其他为失败或异常
     */
    private String code;

    /**
     * 提示信息或异常描述
     */
    private String message;

    /**
     * 返回数据主体
     */
    private T data;

    /**
     * 判断是否为成功返回
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }

}
