package com.mc.core.api;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
/**
 * 响应状态
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public final class ApiResponseState {
	private static final ConcurrentSkipListMap<Integer, String[]> TOTAL = new ConcurrentSkipListMap<Integer, String[]>();;
	private static final Vector<Integer> ALTER = new Vector<Integer>();
	
	public static final ApiResponseState SUCCESS = valueOf(0, "OK");
	public static final ApiResponseState FAILURE = valueOf(-1, "请求执行失败");
	//1xxxx 请求
	public static final ApiResponseState REQUEST_PARAMETER_ILLEGAL1 = valueOf(10001, "请求参数非法");
	public static final ApiResponseState REQUEST_PARAMETER_EMPTY = valueOf(10002, "请求参数为空");
	public static final ApiResponseState PARAMETER_TYPE_ILLEGAL = valueOf(10003, "参数类型非法");
	public static final ApiResponseState ARRAY_LENGTH_EXCEED = valueOf(10004, "数组个数过大");
	public static final ApiResponseState PARAMETER_LENGTH_EXCEED = valueOf(10005, "参数长度过大");
	public static final ApiResponseState PARAMETER_UNDER_LOWER = valueOf(10006, "参数低于下限");
	public static final ApiResponseState PARAMETER_ABOVE_HIGHER = valueOf(10007, "参数高于上限");
	public static final ApiResponseState ARRAY_VALUE_ILLEGAL = valueOf(10008, "数组设值非法");
	public static final ApiResponseState PARAMETER_SIGN_ILLEGAL = valueOf(10009, "参数签名非法");
	
	//2xxxx 权限
	public static final ApiResponseState PARAMETER_TOKEN_INVALID = valueOf(20001, "请求票据无效");
	
	//3xxxx 业务
	public static final ApiResponseState BUSINESS_PRESENT_NONSUPPORT = valueOf(30001, "业务暂不支持");
	public static final ApiResponseState BUSINESS_LOGIC_ERROR = valueOf(30002, "业务逻辑错误");
	public static final ApiResponseState YOUR_OPERATION_ILLEGAL = valueOf(30003, "你的操作非法");
	
	//4xxxx 数据
	public static final ApiResponseState QUERY_DATA_FAILURE = valueOf(40001, "查询数据失败");
	public static final ApiResponseState DATA_EXIST_ERROR = valueOf(40002, "数据存在错误");
	
	//5xxxx 异常
	public static final ApiResponseState UNKOWN_INTERNAL_ERROR = valueOf(50001, "未知内部错误");
	public static final ApiResponseState REQUEST_NETWORK_HURRY = valueOf(50002, "请求网络繁忙");
	/**
	 * 状态代号
	 * */
	private int code;
	/**
	 * 状态信息
	 * */
	private String error;
	/**
	 * 备注说明
	 * */
	private String note;
	/**
	 * 匿名构造方法
	 * */
	private ApiResponseState() {}
	/**
	 * 传参构造方法
	 * @param code 状态代号
	 * @param error 状态信息
	 * @param note 备注说明
	 * */
	private ApiResponseState(int code, String error, String note) {
		this();
		this.code = code;
		this.error = error;
		this.note = note;
	}
	/**
	 * 获取状态代号
	 * */
	public int code() {
		return code;
	}
	/**
	 * 获取状态信息
	 * */
	public String error() {
		return error;
	}
	/**
	 * 备注说明
	 * */
	public String note() {
		return note;
	}
	/**
	 * 修改返回状态
	 * @param code 状态代号
	 * @category 状态代号必须是唯一的，仅能修改一次
	 * */
	public ApiResponseState valueOf(int code) {
		if (this.code == code) {
			return this;
		}
		String[] value = TOTAL.get(code);
		if (value != null) {
			new IllegalArgumentException("该状态已存在");
		}
		if (ALTER.contains(this.code)) {
			new IllegalArgumentException("该状态修改了");
		}
		TOTAL.put(code, TOTAL.get(this.code));
		TOTAL.remove(this.code);
		ALTER.add(code);
		this.code = code;
		return this;
	}
	/**
	 * 修改返回状态
	 * @param note 备注说明
	 * */
	public ApiResponseState valueOf(String note) {
		TOTAL.get(this.code)[1] = note;
		this.note = note;
		return this;
	}
	/**
	 * 修改返回状态
	 * @param error 状态信息
	 * @param note 备注说明
	 * */
	public ApiResponseState valueOf(String error, String note) {
		TOTAL.get(this.code)[0] = error;
		TOTAL.get(this.code)[1] = note;
		this.error = error;
		this.note = note;
		return this;
	}
	/**
	 * 构造返回状态
	 * @param code 状态代号
	 * @param error 状态信息
	 * @category 状态代号必须是唯一的
	 * */
	public static ApiResponseState valueOf(int code, String error) {
		return valueOf(code, error, null);
	}
	/**
	 * 构造返回状态
	 * @param code 状态代号
	 * @param error 状态信息
	 * @param note 备注说明
	 * @category 状态代号必须是唯一的
	 * */
	public static ApiResponseState valueOf(int code, String error, String note) {
		if (TOTAL.containsKey(code)) {
			new IllegalArgumentException("该状态已存在");
		}
		if (error == null || error.equals("")) {
			new IllegalArgumentException("状态信息为空");
		}
		TOTAL.put(code, new String[] { error, note });
		return new ApiResponseState(code, error, note);
	}
	/**
	 * 返回全部状态
	 * */
	public static java.util.Map<Integer, String[]> value() {
		return TOTAL;
	}
}