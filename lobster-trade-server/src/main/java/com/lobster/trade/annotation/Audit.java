package com.lobster.trade.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {
    /** 操作名称，如"处理仲裁"、"修改订单" */
    String value();

    /** 目标类型，如"Order"、"User" */
    String targetType() default "";
}
