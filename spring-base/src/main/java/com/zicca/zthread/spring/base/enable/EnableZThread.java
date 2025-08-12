package com.zicca.zthread.spring.base.enable;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 动态启用 zthread 动态线程池开关注解
 *
 * @author zicca
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MarkerConfiguration.class)
public @interface EnableZThread {
}
