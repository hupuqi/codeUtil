package com.td.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ApiConfig {

    /**
     * 请求类型（默认用于接口请求）
     * */
    @JsonIgnore
    public static final String SHARE_CONSUME = "application/json;charset=UTF-8";

    /**
     * 响应类型（默认用于接口响应）
     * */
    @JsonIgnore
    public static final String SHARE_PRODUCE = "json";
    /**
     * 项目标志（用于上传接口文档）
     * */
    public static Integer PROJECT$MARK;


}
