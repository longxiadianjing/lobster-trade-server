package com.lobster.trade.annotation;

import java.lang.annotation.*;

/**
 * 标注方法需要指定权限才能访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限标识，多个为 OR 关系（即拥有任一即可）
     */
    String[] value();

    /**
     * 是否需要全部权限（默认 false，任一即可）
     */
    boolean all() default false;
}
