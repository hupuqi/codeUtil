package com.mc.core.api;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * 日志切片
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * @see <a href="https://blog.csdn.net/zhanglf02/article/details/78132304">https://blog.csdn.net/zhanglf02/article/details/78132304</a>
 * */
@Aspect
@Component("com.mc.core.api.ApiJournal")
public class ApiJournal {
	private ThreadLocal<LinkedHashMap<String, Object>> POOL = new ThreadLocal<LinkedHashMap<String, Object>>();
	@Pointcut("@annotation(com.mc.core.api.ApiRequestMethod)")
	public void point() {}
	@Around("point()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		try {
			if (POOL.get() == null) {
				Object target = point.getTarget();
				long start = System.currentTimeMillis();
				final LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
				value.put("start", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(new Date(start)));
				value.put("track", 0);
				value.put("state", false);
				value.put("error", null);
				value.put("class.name", target.getClass().getName());
				
				Method method = ((MethodSignature) point.getSignature()).getMethod();
				Parameter[] parameters = method.getParameters();
				Class<?>[] param = new Class<?>[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					param[i] = parameters[i].getType();
				}
				String[] local = new LocalVariableTableParameterNameDiscoverer().getParameterNames(target.getClass().getDeclaredMethod(method.getName(), param));
				
				ArrayList<ArrayList<String>> nozzle = new ArrayList<ArrayList<String>>();
				StringBuffer state = new StringBuffer(ApiRequest.nozzle(method.getModifiers()) + method.getReturnType().getName() + " " + method.getName() + "(");
				for (int i = 0; i < parameters.length; i++) {
					Parameter parameter = parameters[i];
					ArrayList<String> temp = new ArrayList<String>();
					for (Annotation annotation : parameter.getAnnotations()) {
						String name = annotation.annotationType().getName();
						temp.add(name);
						state.append("@" + name + " ");
					}
					nozzle.add(temp);
					state.append(parameter.getParameterizedType().getTypeName() + " " + local[i] + ", ");
				}
				state.deleteCharAt(state.length() - 1);
				state.deleteCharAt(state.length() - 1);
				state.append(")");
				Class<?>[] errors = method.getExceptionTypes();
				if (errors.length > 0) {
					state.append(" throws");
					for (Class<?> error : errors) {
						state.append(" " + error.getName() + ",");
					}
					state.deleteCharAt(state.length() - 1);
				}
				state.append(";");
				
				LinkedHashMap<String, Object> accept = new LinkedHashMap<String, Object>();
				value.put("method.address", null);
				value.put("method.state", state.toString());
				value.put("method.request", new LinkedHashMap<String, Object>());
				value.put("method.response", new LinkedHashMap<String, Object>());
				value.put("method.body", new LinkedHashMap<String, Object>());
				value.put("method.accept", accept);
				
				HttpServletRequest request = null;
				HttpServletResponse response = null;
				ApiRequest body = null;
				boolean inject = false;
				Object[] args = point.getArgs();
				if (args != null && args.length > 0) {
					for (int i = 0; i < args.length; i++) {
						Object arg = args[i];
						if (arg == null) {
							accept.put(local[i], null);
							continue;
						}
						
						if (arg instanceof HttpServletRequest) {
							request = (HttpServletRequest) arg;
							final HttpServletRequest helper = request;
							value.put("method.request", new LinkedHashMap<String, Object>() {
								private static final long serialVersionUID = 1L;
								{
									super.put("AuthType", helper.getAuthType());
									super.put("CharacterEncoding", helper.getCharacterEncoding());
									super.put("ContentLengthLong", helper.getContentLengthLong());
									super.put("ContentType", helper.getContentType());
									super.put("ContextPath", helper.getContextPath());
									super.put("Cookies", helper.getCookies());
									
									LinkedHashMap<String, String> Headers = new LinkedHashMap<String, String>();
									Enumeration<String> HeaderNames = helper.getHeaderNames();
									while (HeaderNames.hasMoreElements()) {
										String key = HeaderNames.nextElement();
										Headers.put(key, helper.getHeader(key));
									}
									super.put("Headers", Headers);
									
									super.put("LocalAddr", helper.getLocalAddr());
									Enumeration<Locale> temp1 = helper.getLocales();
									super.put("LocalName", helper.getLocale().toString());
									ArrayList<String> Locales = new ArrayList<String>();
									while (temp1.hasMoreElements()) {
										Locales.add(temp1.nextElement().toString());
									}
									super.put("Locales", Locales);
									super.put("LocalPort", helper.getLocalPort());
									
									super.put("Method", helper.getMethod().toUpperCase());
									super.put("ParameterMap", helper.getParameterMap());
									super.put("PathInfo", helper.getPathInfo());
									super.put("PathTranslated", helper.getPathTranslated());
									super.put("Protocol", helper.getProtocol());
									
									super.put("QueryString", helper.getQueryString());
									
									super.put("RemoteAddr", helper.getRemoteAddr());
									super.put("RemoteHost", helper.getRemoteHost());
									super.put("RemotePort", helper.getRemotePort());
									super.put("RemoteUser", helper.getRemoteUser());
									super.put("RequestedSessionId", helper.getRequestedSessionId());
									super.put("RequestURL", helper.getRequestURL());
									
									super.put("Scheme", helper.getScheme());
									super.put("ServerName", helper.getServerName());
									super.put("ServerPort", helper.getServerPort());
									super.put("ServletPath", helper.getServletPath());
									
									super.put("UserPrincipal", helper.getUserPrincipal());
								}
							});
						} else if (arg instanceof HttpServletResponse) {
							response = (HttpServletResponse) arg;
							value.put("method.response", this.body(response));
						} else if (arg instanceof ApiRequest) {
							body = (ApiRequest) arg;
							inject = nozzle.get(i).contains(RequestBody.class.getName());
							value.put("method.body", body);
						} else {
							accept.put(local[i], arg);
						}
					}
				}
				
				if (request != null) {
					value.put("method.address", request.getRequestURL().toString());
				}
				if (ApiConfig.AOP$BEFORE != null) {
					ApiRequestMethod api = method.getAnnotation(ApiRequestMethod.class);
					Object res = ApiConfig.AOP$BEFORE.execute(request, response, api == null || api.token(), accept, body, inject, value);
					if (res != null) {
						if (ApiConfig.AOP$AFTER != null && (api == null || api.journal())) {
							value.put("track", System.currentTimeMillis() - start);
							ApiConfig.AOP$AFTER.execute(value, null);
						}
						return res;
					}
				}
				if (ApiConfig.AOP$AFTER != null) {
					POOL.set(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return point.proceed();
	}
	@Before("point()")
	public void before(JoinPoint point) {}
	@After("point()")
	public void after(JoinPoint point) {}
	@AfterReturning(pointcut = "point()", returning = "res")
	public void afterReturning(JoinPoint point, Object res) {
		try {
			long track = System.currentTimeMillis();
			LinkedHashMap<String, Object> value = POOL.get();
			if (ApiConfig.AOP$AFTER == null || value == null) {
				return;
			}
			
			ApiRequestMethod api = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(ApiRequestMethod.class);
			if (api == null || api.journal()) {
				Object[] args = point.getArgs();
				if (args != null && args.length > 0) {
					for (Object arg : args) {
						if (arg != null && arg instanceof HttpServletResponse) {
							value.put("method.response", this.body((HttpServletResponse) arg));
						}
					}
				}
				value.put("state", true);
				value.put("track", track - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").parse(value.get("start").toString()).getTime());
				ApiConfig.AOP$AFTER.execute(value, res);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@AfterThrowing(pointcut = "point()", throwing = "e")
	public void afterThrowing(JoinPoint point, Exception e) {
		try {
			long track = System.currentTimeMillis();
			LinkedHashMap<String, Object> value = POOL.get();
			if (ApiConfig.AOP$AFTER == null || value == null) {
				return;
			}
			
			ApiRequestMethod api = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(ApiRequestMethod.class);
			if (api == null || api.journal()) {
				Object[] args = point.getArgs();
				if (args != null && args.length > 0) {
					for (Object arg : args) {
						if (arg != null && arg instanceof HttpServletResponse) {
							value.put("method.response", this.body((HttpServletResponse) arg));
						}
					}
				}
				
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(error));
				value.put("error", new String(error.toByteArray()));
				value.put("track", track - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").parse(value.get("start").toString()).getTime());
				error.close();
				ApiConfig.AOP$AFTER.execute(value, null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private LinkedHashMap<String, Object> body(HttpServletResponse response) {
		LinkedHashMap<String, Object> res = new LinkedHashMap<String, Object>();
		res.put("BufferSize", response.getBufferSize());
		res.put("CharacterEncoding", response.getCharacterEncoding());
		res.put("ContentType", response.getContentType());
		res.put("Status", response.getStatus());
		return res;
	}
}