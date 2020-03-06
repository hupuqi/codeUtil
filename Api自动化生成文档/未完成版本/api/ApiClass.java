package com.td.api;

import java.lang.annotation.*;

/**
 * 对象注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface ApiClass{
    /**
     * 对象标志
     * @category 取值范围为1-999
     * */
    public int mark() default 0;
    /**
     * 模块名称
     * */
    public String lump() default "";
    /**
     * 对象名称
     * */
    public String title();

    /**
     * 路径
     * 与RequestMapping的机制是一样的
     * 如果用的是spring,值就和RequestMapping的值一样就行
     */
    public String value();
}
