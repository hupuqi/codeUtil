<%@ page language="java" import="
java.text.SimpleDateFormat,
java.util.ArrayList,
java.util.LinkedHashMap,
org.springframework.web.bind.annotation.PathVariable,
org.springframework.web.bind.annotation.RequestBody,
com.fasterxml.jackson.annotation.JsonInclude.Include,
com.fasterxml.jackson.databind.DeserializationFeature,
com.fasterxml.jackson.databind.SerializationFeature,
com.fasterxml.jackson.databind.ObjectMapper,
com.mc.core.api.ApiConfig,
com.mc.core.api.ApiRequest,
com.mc.core.api.ApiResponseState,
com.mc.nozzle.api.ApiJournalAfterImpl,
com.mc.nozzle.api.ApiJournalBeforeImpl" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String ROOT = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
ApiConfig.PROJECT$TOKEN = "";
ApiConfig.NOZZLE$INTACT = false;
ApiConfig.NOZZLE$SHARE = true;
ApiConfig.NOZZLE$PREFIX = ROOT;
ApiConfig.NOZZLE$SUFFIX = ".htm?token={token}";
ApiConfig.NOZZLE$PARAM = new String[] { "token" };
ApiConfig.AOP$BEFORE = new ApiJournalBeforeImpl();
ApiConfig.AOP$AFTER = new ApiJournalAfterImpl();

ArrayList<ApiRequest.Result.ApiMethod> nozzle = ApiRequest.nozzle("com.mc.nozzle.view");
String prefix = ApiConfig.NOZZLE$PREFIX != null ? ApiConfig.NOZZLE$PREFIX : "";
String suffix = ApiConfig.NOZZLE$SUFFIX != null ? ApiConfig.NOZZLE$SUFFIX : "";
StringBuffer query = new StringBuffer();
if (ApiConfig.NOZZLE$PARAM != null && ApiConfig.NOZZLE$PARAM.length > 0) {
	query.append("?");
	for (String name : ApiConfig.NOZZLE$PARAM) {
		String value = request.getParameter(name);
		if (value == null) {
			value = "";
		}
		query.append(name + "=" + value + "&");
		suffix = suffix.replace("{" + name + "}", value);
	}
	query.deleteCharAt(query.length() - 1);
}
%>
<%!
public String html(String data, boolean... tab) {
	if (data == null || data.equals("")) {
		return null;
	}
	if (tab == null || tab.length < 1 || tab[0]) {
		data = data.replace("\t", "").replace("\r", "");
	}
	return data.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("'", "&apos;");
}
public String write(Object data) throws Exception {
	if (data == null) {
		return null;
	}
	ObjectMapper mapper = new ObjectMapper();
	//未知属性
	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	//空的对象
	mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	//是否缩排
	mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	//日期处理
	mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	//枚举处理
	mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
	//枚举顺序
	mapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false);
	//字符数组
	mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
	//空的属性
	mapper.setSerializationInclusion(Include.NON_NULL);
	//空字符串
	mapper.setSerializationInclusion(Include.NON_EMPTY);
	//日期格式
	mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	return mapper.writeValueAsString(data);
}
public String parameter(String consume, ApiRequest.Result.ApiField data, LinkedHashMap<?, String> legal, boolean attach) throws Exception {
	if (ApiConfig.SHARE$FORBID.contains(data.field) && (ApiConfig.NOZZLE$SHARE == null || !ApiConfig.NOZZLE$SHARE)) {
		return "";
	}
	StringBuffer annotion = new StringBuffer();
	for (String[] temp : data.nozzle) {
		annotion.append("@" + temp[0] + " ");
	}
	annotion.append(data.type[0].replace("<", "《").replace(">", "》") + " " + data.field);
	StringBuffer res = new StringBuffer();
	res.append("<tr>");
	res.append("<td>" + (data.name != null ? html(data.name) : "") + "</td>");
	res.append("<td>" + html(data.field) + (data.used != null ? "<del>" + html(data.used) + "</del>" : "") + "</td>");
	res.append("<td colspan=\"2\" data-am-popover=\"{content:'" + annotion + "',trigger:'hover',theme:'sm'}\">" + html(data.type[1]) + " | " + (data.need != null ? (data.need ? "必填" : "选填") : "不限") + " | " + (data.length != null ? data.length + "" : "不限") + "</td>");
	res.append("<td class=\"am-text-center\">" + (data.block != null ? data.block : "") + "</td>");
	
	if (legal != null && legal.size() > 0) {
		StringBuffer item = new StringBuffer();
		for (java.util.Map.Entry<?, String> en : legal.entrySet()) {
			item.append("<br>" + html(en.getKey() + " = " + en.getValue()));
		}
		res.append("<td style=\"padding:0;padding-left:0.7rem;vertical-align:top;\" colspan=\"2\"><div>" + item.substring(4) + "</div></td>");
	} else {
		res.append("<td colspan=\"2\"></td>");
	}
	
	StringBuffer note = new StringBuffer();
	if (data.smaller != null || data.bigger != null) {
		note.append("<br><b>设值范围</b>：" + ((data.smaller != null ? data.smaller + " &lt;= " : "") + "?" + (data.bigger != null ? " &lt; " + data.bigger : "")));
	}
	if (data.note != null) {
		note.append("<br><b>备注说明</b>：" + html(data.note));
	}
	res.append("<td style=\"padding:0;padding-left:0.7rem;vertical-align:top;\" colspan=\"4\">" + (note.length() > 0 ? "<div>" + note.substring(4) + "</div>" : "") + "</td>");
	
	res.append("<td>");
	String type = data.type[0].startsWith(Integer.class.getName()) || data.type[0].startsWith(Long.class.getName()) || data.type[0].startsWith(Double.class.getName()) ? "number" : "text";
	String value = data.layout != null ? html(data.layout) : "";
	String nozzle = attach ? " data-nozzle=\"," + write(data.nozzle).replace("[", "").replace("\"", "").replace(" ", "").replace("]", "") + ",\"" : "";
	if (data.nozzle.contains(PathVariable.class.getName())) {
		res.append("<input type=\"" + type + "\" class=\"am-form-field am-input-sm\"" + nozzle + " name=\"" + data.field + "\">");
	} else {
		if (data.type[0].indexOf("[]") != -1) {
			if (data.type[0].startsWith(Boolean.class.getName()) || data.type[0].startsWith(boolean.class.getName()) || data.type[0].startsWith(Integer.class.getName()) || data.type[0].startsWith(int.class.getName()) || data.type[0].startsWith(Long.class.getName()) || data.type[0].startsWith(long.class.getName()) || data.type[0].startsWith(Double.class.getName()) || data.type[0].startsWith(double.class.getName()) || data.type[0].startsWith(String.class.getName())) {
				res.append("<input type=\"" + type + "\" class=\"am-form-field am-input-sm\" value=\"" + value + "\" data-field=\"" + data.field + "\"" + nozzle + " name=\"" + data.field + "\">");
				res.append("<input type=\"" + type + "\" class=\"am-form-field am-form-field-top am-input-sm\" value=\"" + value + "\" data-field=\"" + data.field + "\"" + nozzle + " name=\"" + data.field + "\">");
				res.append("<input type=\"" + type + "\" class=\"am-form-field am-form-field-top am-input-sm\" value=\"" + value + "\" data-field=\"" + data.field + "\"" + nozzle + " name=\"" + data.field + "\">");
			} else {
				res.append("此页面不支持提交此类型数据");
			}
		} else if (data.type[0].equals(Boolean.class.getName()) || data.type[0].equals(boolean.class.getName()) || data.type[0].equals(Integer.class.getName()) || data.type[0].equals(int.class.getName()) || data.type[0].equals(Long.class.getName()) || data.type[0].equals(long.class.getName()) || data.type[0].equals(Double.class.getName()) || data.type[0].equals(double.class.getName()) || data.type[0].equals(String.class.getName())) {
			res.append("<input type=\"" + type + "\" class=\"am-form-field am-input-sm\" value=\"" + value + "\"" + nozzle + " name=\"" + data.field + "\">");
		} else {
			if ("application/x-www-form-urlencoded".equals(consume)) {
				res.append("此页面不支持提交此类型数据");
			} else {
				String[] format = new String[] { "", "" };
				if (data.type[0].startsWith(java.util.List.class.getName())) {
					format[0] = " data-head=\"[\"";
					format[1] = " data-foot=\"]\"";
				} else if (data.type[0].startsWith(java.util.Map.class.getName())) {
					format[0] = " data-head=\"{\"";
					format[1] = " data-foot=\"}\"";
				}
				res.append("<textarea class=\"am-form-field am-input-sm\" rows=\"3\"" + nozzle + format[0] + format[1] + " name=\"" + data.field + "\">" + value + "</textarea>");
			}
		}
	}
	res.append("</td>");
	res.append("</tr>");
	return res.toString();
}
%>
<!DOCTYPE html>
<html>
<head>
	<title>我的接口</title>
	<base href="<%=ROOT%>">
	<meta charset="UTF-8">
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta name="apple-mobile-web-app-title" content="">
	<meta name="author" content="1138789752@qq.com">
	<meta name="baidu-site-verification" content="SlXWHpfI4o" title="百度资源">
	<meta name="cache-control" content="no-siteapp">
	<meta name="format-detection" content="telephone=no">
	<meta name="mobile-web-app-capable" content="yes">
	<meta name="msapplication-TileColor" content="#0E90D2">
	<meta name="msapplication-TileImage" content="_/i/app-icon72x72@2x.png">
	<meta name="renderer" content="webkit">
	<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
	<meta name="x-ua-compatible" content="IE=edge">
	<link rel="alternate icon" type="image/png" href="favicon.png">
	<link rel="apple-touch-icon-precomposed" type="image/png" href="favicon.png">
	<link rel="icon" type="image/png" href="favicon.png">
	<link rel="stylesheet" type="text/css" href="_/css/amazeui.min-2.6.2.css">
	<link rel="stylesheet" type="text/css" href="_/js/plugin/pace/themes/red/pace-theme-minimal.css">
	<link rel="stylesheet" type="text/css" href="_/js/plugin/rainbow/css/github.css" media="screen">
	<script type="text/javascript" src="_/js/plugin/pace/pace.min-1.0.2.js"></script>
	<script type="text/javascript" src="https://hm.baidu.com/hm.js?4bf7341e2b8f0c76f9645d3299892e25"></script>
<style type="text/css">
#api{margin:6px;}
#api .am-panel-hd{padding:6px 6px 6px 0;}
#api .am-panel-hd .am-panel-title{display:inline-block;}
#api .am-panel-hd .am-margin-right{margin:0;cursor:pointer;}
#api .am-panel-bd{padding:6px;}
#api .am-table{margin:0;border-right:none;border-left:none;}
#api .am-table td{vertical-align:middle;}
#api .am-table td:first-child{border-left:1px solid #DDD;}
#api .am-table td:last-child{border-right:1px solid #DDD;}
#api .am-table a{width:100%;display:block;}
#api .am-table del{color:#DDD;display:block;}
#api .am-table div{max-height:120px;height:120px;overflow-y:auto;}
#api .am-table pre{margin:0;padding:0.7rem;height:240px;overflow-y:auto;}
#api .am-table textarea{min-height:80px;height:100%;line-height:3rem;font-size:2rem;}
#api .am-table .am-form-field{padding:0.3rem;line-height:1.2rem;font-size:1.2rem;}
#api .am-table .am-form-field-top{margin-top:6px;}
#api .am-table .am-btn-sm{width:100%;}
@media (min-height:630px) and (min-width:630px){
	.am-popup{left:30%;width:80%;}
}
</style>
</head>
<body>
<body>
<div class="am-g am-u-sm-12 am-padding-0">
	<div class="am-panel-group" id="api">
	<%
		for (int queue = 0; queue < nozzle.size(); queue ++) {
			ApiRequest.Result.ApiMethod data = nozzle.get(queue);
			String head = "【" + (data.lump != null ? data.lump + " · " : "") + data.title + "】 " + data.name + " - " + data.path.get(0);
			out.print("<div class=\"am-panel am-panel-default\">");
			out.print("<div class=\"am-panel-hd\"><h4 class=\"am-panel-title\" data-am-collapse=\"{parent:\'#api\',target:\'#api-" + queue + "\'}\">" + head + "</h4><i class=\"am-margin-right am-icon-star-o am-fr\"></i></div>");
			out.print("<div class=\"am-collapse am-panel-collapse\" id=\"api-" + queue + "\">");
			out.print("<div class=\"am-panel-bd\">");
			out.print("<form>");
			out.print("<table class=\"am-table am-table-bordered am-table-radius\">");
			
			out.print("<tr class=\"am-primary\">");
			out.print("<td>请求地址：</td>");
			out.print("<td colspan=\"5\">");
			for (int i = 0; i < data.path.size(); i++) {
				out.print("<input type=\"text\" value=\"" + prefix + data.path.get(i) + suffix + "\" class=\"am-form-field" + (i > 0 ? " am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
			}
			out.print("</td>");
			if (data.used.size() > 0) {
				out.print("<td>老版地址：</td>");
				out.print("<td colspan=\"5\">");
				for (int i = 0; i < data.used.size(); i++) {
					out.print("<input type=\"text\" value=\"" + data.used.get(i) + "\" class=\"am-form-field" + (i > 0 ? " am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
				}
				out.print("</td>");
			} else {
				out.print("<td colspan=\"6\"></td>");
			}
			out.print("</tr>");
			
			out.print("<tr class=\"am-primary\">");
			out.print("<td>排序序号：</td>");
			out.print("<td>#" + (queue + 1) + "</td>");
			out.print("<td>需票据吗：</td>");
			out.print("<td>" + (data.token ? "是" : "否") + "</td>");
			out.print("<td>请求方式：</td>");
			out.print("<td headers=\"methodType\" lang=\"" + (data.mode.size() > 0 ? data.mode.get(0) : "") + "\">" + data.mode.toString().replace("[", "").replace(", ", "、").replace("]", "") + "</td>");
			out.print("<td>请求类型 <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'推荐接口调用者使用的请求类型',trigger:'hover',theme:'sm'}\"></i>：</td>");
			out.print("<td colspan=\"3\" headers=\"contentType\" lang=\"" + (data.consume != null ? data.consume.split(";")[0] : "") + "\">" + (data.consume != null ? html(data.consume) : "") + "</td>");
			out.print("<td>响应类型 <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'接口调用者接收响F应数据的默认类型',trigger:'hover',theme:'sm'}\"></i>：</td>");
			out.print("<td headers=\"dataType\" lang=\"" + (data.produce != null ? data.produce : "") + "\">" + (data.produce != null ? html(data.produce) : "") + "</td>");
			out.print("</tr>");
			
			if (data.brief != null) {
				out.print("<tr class=\"am-primary\">");
				out.print("<td>接口简述：</td>");
				out.print("<td colspan=\"11\">" + html(data.brief).replace("\n", "<br>") + "</td>");
				out.print("</tr>");
			}
			
			if (data.jump.size() > 0) {
				out.print("<tr class=\"am-primary\">");
				out.print("<td>相关链接：</td>");
				out.print("<td colspan=\"11\">");
				for (String temp : data.jump) {
					out.print("<a href=\"" + temp + "\" target=\"_blank\">" + html(temp.substring(0, Math.min(120, temp.length()))) + "</a>");
				}
				out.print("</td>");
				out.print("</tr>");
			}
			
			if (ApiConfig.NOZZLE$INTACT != null && ApiConfig.NOZZLE$INTACT) {
				out.print("<tr class=\"am-primary\"><td colspan=\"12\">映射注解（" + (data.mapper[0] != null ? "接口所属对象上的<b data-am-popover=\"{content:'" + data.mapper[0][0] + "',trigger:'hover',theme:'sm'}\">@" + data.mapper[0][1] + "</b> 注解类、": "") + "接口方法申明上的 <b data-am-popover=\"{content:'" + data.mapper[1][0] + "',trigger:'hover',theme:'sm'}\">@" + data.mapper[1][1] + "</b> 注解类）</td></tr>");
				out.print("<tr class=\"am-primary\">");
				out.print("<td>class <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'接口所属对象',trigger:'hover',theme:'sm'}\"></i>：</td>");
				out.print("<td colspan=\"5\"><input type=\"text\" value=\"" + data.clazz + "\" class=\"am-form-field am-input-sm\" readonly=\"readonly\"></td>");
				out.print("<td>method <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'接口方法申明',trigger:'hover',theme:'sm'}\"></i>：</td>");
				out.print("<td colspan=\"5\"><input type=\"text\" value=\"" + data.method + "\" class=\"am-form-field am-input-sm\" readonly=\"readonly\"></td>");
				out.print("</tr>");
				
				String[] title = new String[] { data.mapper[0] != null ? "接口所属对象上的 @" + data.mapper[0][0] + " 注解类中的参数" : "", "接口方法申明上的 @" + data.mapper[1][0] + " 注解类中的参数" };
				ArrayList<ArrayList<String>> headers = data.remove(data.headers);
				if (headers.get(0).size() > 0 || headers.get(1).size() > 0) {
					out.print("<tr class=\"am-primary\">");
					out.print("<td>headers <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[0] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < headers.get(0).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(headers.get(0).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("<td>headers <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[1] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < headers.get(1).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(headers.get(1).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("</tr>");
				}
				
				ArrayList<ArrayList<String>> consumes = data.remove(data.consumes);
				if (consumes.get(0).size() > 0 || consumes.get(1).size() > 0) {
					out.print("<tr class=\"am-primary\">");
					out.print("<td>consumes <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[0] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < consumes.get(0).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(consumes.get(0).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("<td>consumes <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[1] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < consumes.get(1).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(consumes.get(1).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("</tr>");
				}
				
				ArrayList<ArrayList<String>> produces = data.remove(data.produces);
				if (produces.get(0).size() > 0 || produces.get(1).size() > 0) {
					out.print("<tr class=\"am-primary\">");
					out.print("<td>produces <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[0] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < produces.get(0).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(produces.get(0).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("<td>produces <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[1] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < produces.get(1).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(produces.get(1).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("</tr>");
				}
				
				ArrayList<ArrayList<String>> params = data.remove(data.params);
				if (params.get(0).size() > 0 || params.get(1).size() > 0) {
					out.print("<tr class=\"am-primary\">");
					out.print("<td>params <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[0] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < params.get(0).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(params.get(0).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("<td>params <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'" + title[1] + "',trigger:'hover',theme:'sm'}\"></i>：</td>");
					out.print("<td colspan=\"5\">");
					for (int i = 0; i < params.get(1).size(); i++) {
						out.print("<input type=\"text\" value=\"" + html(params.get(1).get(i)) + "\" class=\"am-form-field" + (i > 0 ? "am-form-field-top" : "") + " am-input-sm\" readonly=\"readonly\">");
					}
					out.print("</td>");
					out.print("</tr>");
				}
			}
			
			out.print("<tr class=\"am-success\">");
			out.print("<td>参数名称</td>");
			out.print("<td>字段名称</td>");
			out.print("<td colspan=\"2\">数据类型 | 是否必填 | 数据长度</td>");
			out.print("<td class=\"am-text-center\">数组长度 <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'数组参数的最大个数',trigger:'hover',theme:'sm'}\"></i></td>");
			out.print("<td colspan=\"2\">设值选项 <i class=\"am-icon-question-circle\" data-am-popover=\"{content:'等号左边为可选的参数、等号右边为参数的解释',trigger:'hover',theme:'sm'}\"></i></td>");
			out.print("<td colspan=\"4\">字段说明</td>");
			out.print("<td>测试数据</td>");
			out.print("</tr>");
			
			if (data.share.size() > 0) {
				if (ApiConfig.NOZZLE$INTACT != null && ApiConfig.NOZZLE$INTACT) {
					StringBuffer annotion = new StringBuffer();
					for (String[] temp : data.nozzle) {
						annotion.append("<b data-am-popover=\"{content:'" + temp[0] + "',trigger:'hover',theme:'sm'}\">@" + temp[1] + "</b> ");
					}
					out.print("<tr><td colspan=\"12\">通用参数（接口方法参数中 " + annotion + "<b data-am-popover=\"{content:'" + data.entry[0] + "',trigger:'hover',theme:'sm'}\">" + data.entry[1] + "</b>" + " 参数的 <b data-am-popover=\"{content:'" + ApiRequest.class.getName() + "',trigger:'hover',theme:'sm'}\">" + ApiRequest.class.getSimpleName() + "</b> 父类中的参数）</td></tr>");
				}
				for (ApiRequest.Result.ApiField field : data.share) {
					out.print(parameter(data.consume, field, data.legal.get(field.kagi), false));
				}
			}
			
			if (data.next.size() > 0) {
				if (ApiConfig.NOZZLE$INTACT != null && ApiConfig.NOZZLE$INTACT) {
					StringBuffer annotion = new StringBuffer();
					for (String[] temp : data.nozzle) {
						annotion.append("<b data-am-popover=\"{content:'" + temp[0] + "',trigger:'hover',theme:'sm'}\">@" + temp[1] + "</b> ");
					}
					out.print("<tr><td colspan=\"12\">主体参数（接口方法参数中 " + annotion + "<b data-am-popover=\"{content:'" + data.entry[0] + "',trigger:'hover',theme:'sm'}\">" + data.entry[1] + "</b>" + " 参数）</td></tr>");
				}
				for (ApiRequest.Result.ApiField field : data.next) {
					out.print(parameter(data.consume, field, data.legal.get(field.kagi), false));
				}
			}
			
			if(data.attach.size() > 0) {
				if (ApiConfig.NOZZLE$INTACT != null && ApiConfig.NOZZLE$INTACT) {
					StringBuffer annotion = new StringBuffer();
					if (data.entry != null) {
						for (String[] temp : data.nozzle) {
							annotion.append("<b data-am-popover=\"{content:'" + temp[0] + "',trigger:'hover',theme:'sm'}\">@" + temp[1] + "</b> ");
						}
						annotion.insert(0, "、");
						annotion.append("<b data-am-popover=\"{content:'" + data.entry[0] + "',trigger:'hover',theme:'sm'}\">" + data.entry[1] + "</b>");
					}
					out.print("<tr><td colspan=\"12\">其他参数（接口方法参数中除了 <b data-am-popover=\"{content:'" + HttpServletRequest.class.getName() + "',trigger:'hover',theme:'sm'}\">" + HttpServletRequest.class.getSimpleName() + "</b>、<b data-am-popover=\"{content:'" + HttpServletResponse.class.getName() + "',trigger:'hover',theme:'sm'}\">" + HttpServletResponse.class.getSimpleName() + "</b>" + annotion + " 外其他参数）</td></tr>");
				}
				for (ApiRequest.Result.ApiField field : data.attach) {
					out.print(parameter(data.consume, field, null, true));
				}
			}
			
			for (String path : data.path) {
				out.print("<tr class=\"am-primary\">");
				out.print("<td>可选地址：</td>");
				out.print("<td colspan=\"10\"><input type=\"text\" value=\"" + prefix + path + suffix + "\" class=\"am-form-field am-input-sm\" readonly=\"readonly\"></td>");
				out.print("<td><button type=\"button\" class=\"am-btn am-btn-sm am-btn-primary\" onclick=\"ApiSend(this," + queue + ")\">发送接口请求</button></td>");
				out.print("</tr>");
			}
			out.print("<tr class=\"am-primary\">");
			out.print("<td>请求地址：</td>");
			out.print("<td colspan=\"10\"><input type=\"text\" class=\"am-form-field am-input-sm\" id=\"api-address-" + queue + "\"></td>");
			out.print("<td><button type=\"button\" class=\"am-btn am-btn-sm am-btn-primary\" onclick=\"ApiCode('" + head + "'," + data.id + ",'" + prefix + data.path.get(0) + suffix + "')\">查看测试代码</button></td>");
			out.print("</tr>");
			
			out.print("<tr class=\"am-primary\">");
			out.print("<td>请求参数：</td>");
			out.print("<td colspan=\"3\"><pre id=\"api-request-" + queue + "\"></pre></td>");
			out.print("<td>响应数据 <i class=\"am-icon-question-circle\" onclick=\"ApiTemplate('" + head + "'," + queue + ")\"></i>：</td>");
			out.print("<td colspan=\"7\">");
			out.print("<pre class=\"am-hide\" id=\"api-template-" + queue + "\"></pre>");
			out.print("<pre id=\"api-response-" + queue + "\">" + (data.body != null ? ApiRequest.format(data.body, String.class, true) : "") + "</pre>");
			out.print("</td>");
			out.print("</tr>");
			
			out.print("</table>");
			out.print("</form>");
			out.print("</div>");
			out.print("</div>");
			out.print("</div>");
		}
		
		out.print("<div class=\"am-panel am-panel-default\">");
		out.print("<div class=\"am-panel-hd\"><h4 class=\"am-panel-title\" data-am-collapse=\"{parent:\'#api\',target:\'#api-" + nozzle.size() + "\'}\">【响应状态】错误码</h4><i class=\"am-margin-right am-icon-star-o am-fr\"></i></div>");
		out.print("<div class=\"am-collapse am-panel-collapse\" id=\"api-" + nozzle.size() + "\">");
		out.print("<div class=\"am-panel-bd\">");
		out.print("<table class=\"am-table am-table-bordered am-table-radius\">");
		out.print("<tr class=\"am-success\">");
		out.print("<td>状态代号</td>");
		out.print("<td colspan=\"4\">状态信息</td>");
		out.print("<td colspan=\"7\">备注说明</td>");
		out.print("</tr>");
		out.print("<tr><td colspan=\"12\">1xxxx = 请求类&nbsp;&nbsp;&nbsp;2xxxx = 权限类&nbsp;&nbsp;&nbsp;3xxxx = 业务类&nbsp;&nbsp;&nbsp;4xxxx = 数据类&nbsp;&nbsp;&nbsp;5xxxx = 异常类</td></tr>");
		for (java.util.Map.Entry<Integer, String[]> en : ApiResponseState.value().entrySet()) {
			String[] value = en.getValue();
			out.print("<tr>");
			out.print("<td>" + en.getKey() + "</td>");
			out.print("<td colspan=\"4\">" + value[0] + "</td>");
			out.print("<td colspan=\"7\">" + (value[1] != null ? value[0] : "") + "</td>");
			out.print("</tr>");
		}
		out.print("</table>");
		out.print("</div>");
		out.print("</div>");
		out.print("</div>");
	%>
	</div>
</div>
<div style="padding-bottom:0;height:auto;" class="am-popup" id="template">
	<div class="am-popup-inner">
		<div class="am-popup-hd">
			<h4 class="am-popup-title"></h4>
			<i class="am-close am-close-spin" data-am-modal-close>&times;</i>
		</div>
		<div class="am-popup-bd">
			<form>
				<div class="am-input-group am-input-group-success">
					<div class="am-input-group-label">响应数据</div>
					<textarea style="width:100%;height:240px;" class="am-form-field" readonly="readonly"></textarea>
				</div>
			</form>
		</div>
	</div>
</div>
<div class="am-popup" id="code">
	<div class="am-popup-inner" id="code-inner">
		<div class="am-popup-hd">
			<h4 class="am-popup-title"></h4>
			<i class="am-close am-close-spin" data-am-modal-close>&times;</i>
		</div>
		<div class="am-padding-0 am-popup-bd"></div>
	</div>
</div>
<script type="text/javascript" src="_/js/jquery.min-2.2.3.js"></script>
<script type="text/javascript" src="_/js/amazeui.min-2.6.2.js"></script>
<script type="text/javascript" src="_/js/plugin/rainbow/rainbow.min-2.1.2.js"></script>
<script type="text/javascript" src="_/js/plugin/rainbow/lang/generic.js"></script>
<script type="text/javascript" src="_/js/plugin/rainbow/lang/html.js"></script>
<script type="text/javascript" src="_/js/plugin/rainbow/lang/java.js"></script>
<script type="text/javascript" src="_/js/plugin/rainbow/lang/javascript.js"></script>
<script type="text/javascript">
function ApiFormat(data){
	var param={LineAfterColon:false,SpaceAfterColon:true};
	if(typeof data!=="string"){
		data=JSON.stringify(data);
	}
	data=JSON.stringify(JSON.parse(data));
	data=data.replace(/([\{\}])/g,"\r\n$1\r\n").replace(/([\[\]])/g,"\r\n$1\r\n").replace(/(\,)/g,"$1\r\n").replace(/(\r\n\r\n)/g,"\r\n").replace(/\r\n\,/g,",");
	if(!param.LineAfterColon){
		data=data.replace(/\:\r\n\{/g,":{").replace(/\:\r\n\[/g,":[");
	}
	if(param.SpaceAfterColon){
		data=data.replace(/\:/g,": ");
	}
	var res="",count=0;
	$.each(data.split("\r\n"),function(index,node){
		var indent=0;
		if(node.match(/\{$/)||node.match(/\[$/)){
			indent=1;
		}else if(node.match(/\}/)||node.match(/\]/)){
			if(count!==0){
				count-=1;
			}
		}else{
			indent=0;
		}
		for(var i=0;i<count;i++){
			res+="    ";
		}
		count+=indent;
		res+=node+"\r\n";
	});
	return res.substring(2,res.length-4).replace(/http: \/\//g,"http://").replace(/https: \/\//g,"https://").replace(/: 8088/g,":8088");
}
function ApiSend(that,queue){
	var api=$("#api-"+queue);
	var contentType=api.find("td[headers='contentType']").attr("lang").toLocaleLowerCase();
	if(contentType!="application/x-www-form-urlencoded"&&contentType!="application/json"&&!window.confirm("本页目前仅支持：\n    application/x-www-form-urlencoded\n    application/json\n等Content-Type类型。你还要继续吗？")){
		return;
	}
	var form=$("#api-"+queue+" form");
	var methodType=api.find("td[headers='methodType']").attr("lang");
	var url=$(that).closest("tr").find("td:eq(1)").find("input").val();
	var dataType=api.find("td[headers='dataType']").attr("lang");
	var address=$("#api-address-"+queue);
	var request=api.find("#api-request-"+queue);
	var template=api.find("#api-template-"+queue);
	var response=api.find("#api-response-"+queue);
	var en={};
	$.each(api.find("[name]"),function(){
		var o=$(this);
		var v=o.val();
		if(!v){
			return true;
		}
		try{
			if(this.tagName.toLowerCase()=="textarea"){
				var format=[o.data("head"),o.data("foot")];
				if(format[0]&&v.indexOf(format[0])!=0||format[1]&&v.substring(v.length-format[1].length)!=format[1]){
					window.alert("参数格式非法");
					return false;
				}
				eval("v="+v);
			}
		}catch(e){
			en=null;
			window.alert("参数格式非法");
			return false;
		}
		var n=o.attr("name");
		var z=o.data("nozzle");
		var k=o.data("field");
		if(z){
			if(z.indexOf(",<%=PathVariable.class.getName()%>,")!=-1){
				url=url.replace("{"+n+"}",v);
			}else{
				if(contentType=="application/json"){
					if(z.indexOf(",<%=RequestBody.class.getName()%>,")!=-1){
						en=v;
					}else{
						url+=(url.indexOf("?")!=-1?"&":"?")+(k?k:n)+"="+v;
					}
				}
			}
		}else{
			if(k){
				if(en.hasOwnProperty(k)){
					en[k].push(v);
				}else{
					en[k]=[v];
				}
			}else{
				en[n]=v;
			}
		}
	});
	if(!en){
		return;
	}
	var data=null;
	if(contentType.indexOf("application/x-www-form-urlencoded")==0){
		en=form.serialize();
		data=en;
	}else if(contentType.toLocaleLowerCase().indexOf("application/json")==0){
		if(typeof en=="string"){
			data=en;
		}else{
			en=JSON.stringify(en);
			data=ApiFormat(en);
		}
	}else{
		en=JSON.stringify(en);
		data=ApiFormat(en);
	}
	$.ajax({
		type:methodType,
		url:url,
		data:en,
		dataType:(dataType?dataType:null),
		contentType:(contentType?contentType:null),
		async:false,
		beforeSend:function(){
			if(!template.html()){
				template.html(response.html());
			}
			address.val(url);
			request.html(data);
			response.html(ApiFormat({}));
		},
		success:function(res){
			response.html(ApiFormat(res));
		},
		error:function(request,message,error){
			var data={
				XMLHttpRequest:{
					readyState:request.readyState,
					status:request.status,
					statusText:request.statusText
				},
				textStatus:message,
				errorThrown:error
			};
			response.html(ApiFormat(data));
		}
	});
}
function ApiCode(head,id,path){
	$("#code .am-popup-title").html("测试代码（"+head+"）");
	$("#code .am-popup-bd").remove();
	if(!id){
		$("#code .am-popup-inner").append("<div class=\"am-padding-0 am-popup-bd\"><pre class=\"am-margin-0\" data-language=\"java\">//暂无测试代码</pre></div>");
		$("#code").modal();
		return;
	}
	$.ajax({
		type:"get",
		url:"http://www.ink10000.com/api/code.htm?token=<%=ApiConfig.PROJECT$TOKEN%>",
		data:{id:id,path:path},
		dataType:"jsonp",
		async:false,
		success:function(res){
			var div=document.createElement("div");
			div.className="am-padding-0 am-popup-bd";
			div.innerHTML=res.data;
			Rainbow.color(div,function(){
				var o=document.getElementById("code-inner");
				o.appendChild(div);
				$("#code").modal();
			});
		},
		error:function(){
			$("#code .am-popup-bd").html("<pre class=\"am-margin-0\" data-language=\"java\">//请求接口失败</pre>");
			$("#code").modal();
		}
	});
}
function ApiTemplate(head,queue){
	var data=$("#api-template-"+queue).html();
	if(!data){
		data=$("#api-response-"+queue).html();
		if(!data){
			return;
		}
	}
	$("#template .am-popup-title").html("响应数据（"+head+"）");
	$("#template textarea").val(data);
	$("#template").modal();
}
$(document).ready(function(){
	var search=document.location.search;
	var query="<%=query%>";
	if(query){
		var href=location.href;
		if(search){
			if(href.indexOf(query)!=href.length-query.length){
				window.history.pushState({},document.title,href.replace(search,"")+query);
			}
		}else{
			window.history.pushState({},document.title,href+query);
		}
	}else{
		if(search){
			window.history.pushState({},document.title,location.href.replace(search,""));
		}
	}
	$("pre[id^='api-request-']").html("<b>请先点击右侧发送请求按钮</b>");
	$("#api").on("opened.collapse.amui",function(e){
		$.each($(e.target).find("textarea"),function(){
			var o=$(this);
			var t=o.parent("td");
			o.height(t.height()-parseInt(t.css("padding-top"))-parseInt(t.css("padding-bottom")));
		});
	});
	$.each($("#api td:not(:last-child)"),function(){
		var o=$(this);
		var c=parseInt(o.attr("colspan"));
		if(!c){
			c=1;
		}
		o.css({"max-width":c*8.2+"%","width":c*8.2+"%"});
	});
	$("#api input[readonly],#template textarea").focus(function(e){
		try{
			$(this).select();
			document.execCommand("Copy");
		}catch(e){}
	});
	$("#api pre").dblclick(function(e){
		try{
			var text=document.getElementById($(this).attr("id"));
			if(document.body.createTextRange){
				var range=document.body.createTextRange();
				range.moveToElementText(text);
				range.select();
			}else if(window.getSelection){
				var range=document.createRange();
				range.selectNodeContents(text);
				var selection=window.getSelection();
				selection.removeAllRanges();
				selection.addRange(range);
			}else{
				return;
			}
			document.execCommand("Copy");
		}catch(e){}
	});
	$("#api [data-am-popover]").on("mouseover",function(){
		var o=$(this);
		var p=null;
		eval("p="+o.data("am-popover"));
		var c=p.content;
		if(c.indexOf("《")!=-1||c.indexOf("》")!=-1){
			o.popover("setContent",c.replace("《","&lt;").replace("》","&gt;"));
		}
	});
});















window.onerror=function(msg,url,line){
	if(url.indexOf("amazeui.min-2.6.2.js")!=-1||url.indexOf("ueditor.all.min-1.4.3.3.js")!=-1){
		return true;
	}
	var r="页面报错！\n";
	r+="错误信息:"+msg+"\n";
	r+="出错url:"+url+"\n";
	r+="出错行数:"+line+"\n";
	r+="点击继续";
	window.alert(r);
	return true;
};
</script>
</body>
</html>