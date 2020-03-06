package com.mc.core.api;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 之前切片
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public interface ApiJournalBefore {
	/**
	 * 对参数预处理
	 * @param request 请求对象
	 * @param response 响应对象
	 * @param token 需票据吗
	 * @param accept 方法参数
	 * @param body 主体参数
	 * @param inject 注入了吗，主体参数前是有 @{@link org.springframework.web.bind.annotation.RequestBody}
	 * @param data 日志数据
	 * */
	public Object execute(HttpServletRequest request, HttpServletResponse response, boolean token, LinkedHashMap<String, Object> accept, ApiRequest body, boolean inject, LinkedHashMap<String, Object> data);
}