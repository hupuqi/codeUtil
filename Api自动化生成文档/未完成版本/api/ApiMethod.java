package com.td.api;

import java.lang.annotation.*;

/**
 * 请求方法
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * @category 接口方法上处理数据类型为：<br>
 * 	数组 ?[]，其中 ? 仅限Boolean、boolean、Integer、int、Long、long、Double、double、String、自定义类<br>
 * 	注解 @PathVariable ?、@PathVariable ?[]、@RequestParam ?、@RequestParam ?[]，其中 ? 仅限Boolean、boolean、Integer、int、Long、long、Double、double、String<br>
 * 	接口List&lt;?&gt;实现类，其中 ? 仅限Boolean、boolean、Integer、int、Long、long、Double、double、String、Object、自定义类<br>
 * 	接口Map&lt;?, ?&gt;实现类，其中 ? 仅限Boolean、boolean、Integer、int、Long、long、Double、double、String、Object
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface ApiMethod {
	/**
	 * 接口标志
	 * @category 取值范围为1-999，项目标志 + 对象标志 * 1000 + 接口标志 = 接口唯一编号，该编号与接口测试代码相关联，一旦设置了最好不要修改
	 * */
	public int mark() default 0;
	/**
	 * 接口标题
	 * */
	public String name();
	/**
	 * 老版地址
	 * */
	public String[] used() default {};
	/**
	 * 需票据吗
	 * @category 默认为true
	 * */
	public boolean token() default true;

	/**
	 * 请求类型
	 * @see {@link org.springframework.http.MediaType}
	 * @category 推荐接口调用者请求时ContentType
	 * */
	public String consume() default ApiConfig.SHARE_CONSUME;
	/**
	 * 响应类型
	 * @category 接口调用者接收返回数据的默认类型
	 * */
	public String produce() default ApiConfig.SHARE_PRODUCE;
	/**
	 * 接口简述
	 * */
	public String brief() default "";
	/**
	 * 相关链接
	 * */
	public String[] jump() default {};
	/**
	 * 排列序号
	 * */
	public int queue() default 1;

	/**
	 * 路径
	 * 与RequestMapping的机制是一样的
	 * 如果用的是spring,值就和RequestMapping的值一样就行
	 */
	public String value();

	/**
	 * 请求方式
	 */
	public RequestMethodMode methodMode() default RequestMethodMode.POST;

	/**
	 * 接口返回的参数类
	 * @return
	 */
	public Class<?>[] returnParamEntity() default {};

	

}