package com.zicca.zthread.dashboard.dev.starter.core;

import static com.zicca.zthread.dashboard.dev.starter.core.Result.ERROR_CODE;
import static com.zicca.zthread.dashboard.dev.starter.core.Result.SUCCESS_CODE;

/**
 * 返回结果构造工具类
 * 提供统一的成功/失败返回构造方法，简化 Result 对象的创建
 *
 * @author zicca
 */
public final class Results {

    /**
     * 创建一个无数据的成功响应
     *
     * @return 包含成功状态码的 Result 对象，data 为 null
     */
    public static Result<Void> success() {
        return new Result<Void>().setCode(SUCCESS_CODE);
    }

    /**
     * 创建一个有数据的成功响应
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     * @return 包含成功状态码的 Result 对象，data 为指定数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>().setCode(SUCCESS_CODE).setData(data);
    }

    /**
     * 创建一个失败的响应
     *
     * @return 默认失败响应
     */
    public static Result<Void> fail() {
        return fail("Internal error");
    }

    /**
     * 创建一个失败的响应
     *
     * @param message 失败信息
     * @return 默认失败响应
     */
    public static Result<Void> fail(String message) {
        return new Result<Void>().setCode(ERROR_CODE).setMessage(message);
    }

    /**
     * 创建一个失败的响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 失败响应
     */
    public static Result<Void> fail(String code, String message) {
        return new Result<Void>().setCode(code).setMessage(message);
    }

}
