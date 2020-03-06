package com.mc.core.api;
import java.util.LinkedHashMap;
/**
 * 之后切片
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public interface ApiJournalAfter {
	/**
	 * 存储切片日志
	 * @param data 日志数据
	 * @param res 返回数据，方法执行成功时返回的数据
	 * */
	public void execute(LinkedHashMap<String, Object> data, Object res);
}