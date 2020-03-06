package com.mc.core.api;
import java.util.LinkedHashMap;
/**
 * 响应数据
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public interface ApiResponse {
	/**
	 * 获取响应状态
	 * */
	public int getState();
	/**
	 * 设置成功状态
	 * */
	public ApiResponse setState();
	/**
	 * 设置失败状态
	 * @param error 错误对象
	 * */
	public ApiResponse setState(Object error);
	/**
	 * 设置响应状态
	 * @param code 状态代号
	 * @param error 错误信息
	 * */
	public ApiResponse setState(int code, String error);
	/**
	 * 设置响应状态
	 * @param code 状态代号
	 * @param error 错误信息
	 * */
	public ApiResponse setState(ApiResponseState code, String error);
	/**
	 * 获取备注说明
	 * @param note 备注说明
	 * */
	public String getNote();
	/**
	 * 设置备注说明
	 * @param note 备注说明
	 * */
	public ApiResponse setNote(String note);
	/**
	 * 设置请求参数
	 * @param body 参数对象
	 * */
	public ApiResponse setArgs(ApiRequest body);
	/**
	 * 获取返回主体
	 * @param data 主体数据
	 * */
	public LinkedHashMap<String, Object> getBody();
	/**
	 * 设置返回主体
	 * @param data 主体数据
	 * */
	public ApiResponse setBody(LinkedHashMap<String, Object> data);
	/**
	 * 设置分页信息
	 * @param pager 分页模式，当取值为<b>仅设页号</b>、<b>传统分页</b>时，参数<b>total</b>、<b>size</b>、<b>tabs</b>、<b>now</b>有效；当取值为<b>分隔符分页</b>时，参数<b>size</b>、<b>marker</b>有效
	 * @param total 总记录数
	 * @param size 每页条数
	 * @param tabs 总分页数
	 * @param now 当前页号
	 * @param marker 分页标识
	 * */
	public ApiResponse setPage(ApiRequest.ApiLeafer pager, Long total, Long size, Long tabs, Long now, String marker);
	/**
	 * 获取其他参数
	 * @category 推荐此参数用于日志切片中
	 * */
	public Object getEtc();
	/**
	 * 设置其他参数
	 * @category 推荐此参数用于日志切片中
	 * */
	public ApiResponse setEtc(Object data);
}