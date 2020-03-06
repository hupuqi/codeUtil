package com.td.api;

import java.lang.annotation.*;

/**
 * 字段注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD})
@Documented
public @interface ApiField {
    /**
     * 参数名称
     * */
    public String name();

    /**
     * 最大长度
     */
    public int maxLength() default 0;

    /**
     * 最小长度
     */
    public int minLength() default 0;

    /**
     * 备注说明
     */
    public String note() default "";

    /**
     * 是否必填
     * @category 默认为false
     * */
    public boolean need() default false;

}
