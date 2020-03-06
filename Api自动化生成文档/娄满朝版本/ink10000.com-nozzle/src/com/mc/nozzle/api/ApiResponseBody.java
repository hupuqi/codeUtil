package com.mc.nozzle.api;
import java.util.LinkedHashMap;
import com.mc.core.api.ApiRequest;
import com.mc.core.api.ApiResponse;
import com.mc.core.api.ApiResponseState;
/**
 * 响应数据
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
public class ApiResponseBody extends LinkedHashMap<String, Object> implements ApiResponse {
	public static final long serialVersionUID = System.identityHashCode(ApiResponseBody.class);
	private final LinkedHashMap<String, Object> args = new LinkedHashMap<String, Object>();
	private final LinkedHashMap<String, Object> body = new LinkedHashMap<String, Object>();
	private final LinkedHashMap<String, Object> leaf = new LinkedHashMap<String, Object>();
	private ApiRequest request;
	private Object data;
	public ApiResponseBody() {
		super.put("code", ApiResponseState.FAILURE.code());
		super.put("error", "NO");
		super.put("note", "若有问题，请+QQ1138789752。");
		super.put("args", args);
		super.put("body", body);
		super.put("leaf", leaf);
	}
	public ApiResponseBody(ApiRequest request) {
		this();
		this.request = request;
	}
	@Override
	public Object get(Object key) {
		return body.get(key);
	}
	@Override
	public Object put(String key, Object value) {
		return body.put(key, value);
	}
	public int getState() {
		return (Integer) super.get("code");
	}
	public ApiResponseBody setState() {
		this.setState(ApiResponseState.SUCCESS.code(), "OK");
		return this;
	}
	public ApiResponseBody setState(Object error) {
		if (error instanceof ApiResponseState) {
			ApiResponseState state = (ApiResponseState) error;
			this.setState(state.code(), state.error());
		} else if (error instanceof Throwable) {
			Throwable e = (Throwable) error;
			e.printStackTrace();
			this.setState(ApiResponseState.UNKOWN_INTERNAL_ERROR.code(), e.getMessage() != null ? e.getMessage() : ApiResponseState.UNKOWN_INTERNAL_ERROR.error());
		} else {
			this.setState(ApiResponseState.FAILURE.code(), error.toString());
		}
		return this;
	}
	public ApiResponseBody setState(int code, String error) {
		super.put("code", code);
		super.put("error", error);
		if (code != 0) {
			this.setBody(new LinkedHashMap<String, Object>());
		}
		if (request != null) {
			this.setArgs(request);
		}
		return this;
	}
	public ApiResponseBody setState(ApiResponseState code, String error) {
		this.setState(code.code(), error);
		return this;
	}
	public String getNote() {
		return (String) super.get("note");
	}
	public ApiResponseBody setNote(String note) {
		super.put("note", note);
		return this;
	}
	@SuppressWarnings("unchecked")
	public ApiResponseBody setArgs(ApiRequest body) {
		try {
			if (body.SHARE$ARGS != null && body.SHARE$ARGS) {
				args.clear();
				if (body.SHARE$SACK.size() > 0) {
					args.put("SHARE$SACK", body.SHARE$SACK);
				}
				args.putAll(ApiRequest.format(ApiRequest.format(body, String.class), LinkedHashMap.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	public LinkedHashMap<String, Object> getBody() {
		return body;
	}
	public ApiResponseBody setBody(LinkedHashMap<String, Object> data) {
		this.body.clear();
		this.body.putAll(data);
		return this;
	}
	public ApiResponseBody setPage(ApiRequest.ApiLeafer pager, Long total, Long size, Long tabs, Long now, String marker) {
		if (pager == null) {
			return this;
		}
		if (pager == ApiRequest.ApiLeafer.NUMBER_LEAFER
			|| pager == ApiRequest.ApiLeafer.GENERAL_LEAFER) {
			leaf.clear();
			if (total != null && total > -1
				&& size != null && size > -1
				&& tabs != null && tabs > -1
				&& now != null && now > -1) {
				leaf.put("SHARE$TOTAL", total);
				leaf.put("SHARE$SIZE", size);
				leaf.put("SHARE$TABS", tabs);
				leaf.put("SHARE$NOW", now);
			}
		} else if (pager == ApiRequest.ApiLeafer.MARKER_LEAFER) {
			leaf.clear();
			if (size != null && size > -1
				&& marker != null && !marker.equals("")) {
				leaf.put("SHARE$SIZE", size);
				leaf.put("SHARE$MARKER", marker);
			}
		}
		return this;
	}
	public Object getEtc() {
		return data;
	}
	public ApiResponse setEtc(Object data) {
		this.data = data;
		return this;
	}
}