package com.zicca.zthread.spring.base;

import java.lang.annotation.*;

/**
 * 动态线程池注解
 *
 * @author zicca
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {
}
