package com.mc.nozzle.api;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mc.core.api.ApiJournalBefore;
import com.mc.core.api.ApiRequest;
import com.mc.core.api.ApiResponse;
/**
 * 之前切片（默认实现）
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public class ApiJournalBeforeImpl implements ApiJournalBefore {
	public Object execute(HttpServletRequest request, HttpServletResponse response, boolean token, LinkedHashMap<String, Object> accept, ApiRequest body, boolean inject, LinkedHashMap<String, Object> data) {
		try {
			ApiResponse res = new ApiResponseBody();
			if (body != null) {
				res.setArgs(body);
				if (inject ? body.verify(res) : body.verify(request, res)) {
					return res;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}