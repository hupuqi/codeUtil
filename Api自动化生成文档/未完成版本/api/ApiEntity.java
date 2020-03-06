package com.td.api;

import java.lang.annotation.*;

/**
 * 实体注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface ApiEntity {

}
