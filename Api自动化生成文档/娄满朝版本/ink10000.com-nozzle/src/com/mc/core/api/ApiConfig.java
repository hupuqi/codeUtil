package com.mc.core.api;
import java.util.Arrays;
import java.util.Comparator;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * 全局配置
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public class ApiConfig {
	/**
	 * 无效返回（被忽略的返回类型）
	 * */
	@JsonIgnore
	public static final java.util.List<Class<?>> SHARE$REVERT = Arrays.asList(void.class, ModelAndView.class);
	/**
	 * 父级共参（禁止子类定义属性）
	 * */
	@JsonIgnore
	public static final java.util.List<String> SHARE$FORBID = Arrays.asList("SHARE$SIZE_DEF", "SHARE$SIZE_MAX", "SHARE$CONSUME", "SHARE$PRODUCE", "SHARE$FORBID", "SHARE$SACK", "SHARE$ARGS", "SHARE$FIELD", "SHARE$ORDER", "SHARE$QUEUE", "SHARE$SIZE", "SHARE$NOW", "SHARE$MARKER");
	/**
	 * 每页条数（默认用于数据分页）
	 * */
	@JsonIgnore
	public static final Long SHARE$SIZE_MAX = 500L;
	/**
	 * 请求类型（默认用于接口请求）
	 * */
	@JsonIgnore
	public static final String SHARE$CONSUME = MediaType.APPLICATION_JSON_UTF8_VALUE;
	/**
	 * 响应类型（默认用于接口响应）
	 * */
	@JsonIgnore
	public static final String SHARE$PRODUCE = "json";
	/**
	 * 项目标志（用于上传接口文档）
	 * */
	public static Integer PROJECT$MARK;
	/**
	 * 请求票据（用于上传接口文档）
	 * */
	public static String PROJECT$TOKEN;
	/**
	 * 完整模式（是否显示完整文档）
	 * */
	public static Boolean NOZZLE$INTACT;
	/**
	 * 有共参吗（是否显示父级共参）
	 * */
	public static Boolean NOZZLE$SHARE;
	/**
	 * 接口前缀（接口请求地址前缀）
	 * */
	public static String NOZZLE$PREFIX;
	/**
	 * 接口后缀（接口请求地址后缀）
	 * */
	public static String NOZZLE$SUFFIX;
	/**
	 * 文档参数（文档地址上的参数）
	 * */
	public static String[] NOZZLE$PARAM;
	/**
	 * 排序对象（对接口排序的实现）
	 * */
	public static Comparator<ApiRequest.Result.ApiMethod> NOZZLE$COMPARATOR;
	/**
	 * 之前切片（进入方法体前切片）
	 * */
	@JsonIgnore
	public static ApiJournalBefore AOP$BEFORE;
	/**
	 * 之后切片（进入方法体前切片）
	 * */
	@JsonIgnore
	public static ApiJournalAfter AOP$AFTER;
	/**
	 * 解析注解（参数中第三方注解）
	 * */
	@JsonIgnore
	public static ApiRequestField DOC$VERIFTY;
}