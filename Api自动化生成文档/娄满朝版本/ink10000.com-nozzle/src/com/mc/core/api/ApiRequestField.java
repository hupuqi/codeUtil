package com.mc.core.api;
import java.lang.reflect.Field;
/**
 * 解析注解
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public interface ApiRequestField {
	/**
	 * 解析其他注解
	 * @param field 主体参数
	 * @param res 返回参数
	 * @category 生成接口文档时，解析第三方校检注解
	 * */
	public void verifty(Field field, ApiRequest.Result.ApiField res);
}