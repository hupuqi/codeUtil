package com.mc.core.api;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
/**
 * 请求数据
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * @category 此类处理数据类型为：<br>
 * 	简单类参数仅限Boolean、Integer、Long、Double、String；<br>
 * 	数组类参数仅限Boolean[]、Integer[]、Long[]、Double[]、String[]；<br>
 * 	集合类参数仅限List&lt;?&gt;、Map&lt;?, ?&gt;，其中 ? 仅限Boolean、Integer、Long、Double、String、Object类型；<br>
 * 	其他类型参数可根据具体业务进行相应修改
 * */
public abstract class ApiRequest {
	/**
	 * 无效参数
	 * */
	@JsonIgnore
	public final LinkedHashMap<String, Object> SHARE$SACK = new LinkedHashMap<String, Object>();
	/**
	 * 返参数吗
	 * */
	public Boolean SHARE$ARGS;
	/**
	 * 查询字段
	 * */
	public String[] SHARE$FIELD;
	/**
	 * 排序字段
	 * */
	public String SHARE$ORDER;
	/**
	 * 是否升序
	 * */
	public Boolean SHARE$QUEUE;
	/**
	 * 每页条数
	 * */
	public Long SHARE$SIZE;
	/**
	 * 当前页号
	 * */
	public Long SHARE$NOW;
	/**
	 * 分页标识
	 * */
	public String SHARE$MARKER;
	/**
	 * 忽略标识
	 * */
	public String[] SHARE$KEYS;
	/**
	 * 注入无效字段
	 * */
	@JsonAnySetter
	public void setter(String key, Object value) {
		SHARE$SACK.put(key, value);
	}
	/**
	 * 检校主体参数
	 * @param type 参数类型
	 * @param name 参数字段
	 * @param value 参数的值
	 * @param verify 参数注解
	 * @param legal 取值集合
	 * @param res 响应数据
	 * */
	private Object verify(Class<?> type, String name, Object value, ApiField verify, LinkedHashMap<String, LinkedHashMap<?, String>> legal, ApiResponse res) throws Exception {
		if (value == null || value.toString().equals("")) {
			if (verify.need()) {
				res.setState(ApiResponseState.REQUEST_PARAMETER_EMPTY, verify.name() + "(" + name + ")不能设置为空");
				return null;
			} else {
				return verify.empty() ? null : value;
			}
		}
		
		if (type == Integer.class || type == Long.class) {
			long temp = Long.parseLong(value.toString());
			if (verify.smaller() != Long.MIN_VALUE && verify.smaller() > temp) {
				res.setState(ApiResponseState.PARAMETER_UNDER_LOWER, verify.name() + "(" + name + ")参数低于下限");
				return null;
			}
			if (verify.bigger() != Long.MAX_VALUE && verify.bigger() <= temp) {
				res.setState(ApiResponseState.PARAMETER_ABOVE_HIGHER, verify.name() + "(" + name + ")参数高于上限");
				return null;
			}
		}
		
		if (!verify.kagi().equals("")) {
			LinkedHashMap<?, String> temp = legal.get(verify.kagi());
			if (temp == null) {
				res.setState(ApiResponseState.BUSINESS_LOGIC_ERROR, verify.name() + "(" + name + ")参数解析错误");
				return null;
			}
			if (!temp.containsKey(value)) {
				res.setState(ApiResponseState.ARRAY_VALUE_ILLEGAL, verify.name() + "(" + name + ")参数取值非法");
				return null;
			}
		}
		return value;
	}
	/**
	 * 检校主体参数
	 * @param res 响应数据
	 * @category 当验证失败时返回true，调用此方法时参数前需有 {@link org.springframework.web.bind.annotation.RequestBody} 注解类
	 * */
	@SuppressWarnings("unchecked")
	public boolean verify(ApiResponse res) {
		try {
			if (res == null) {
				return true;
			}
			LinkedHashMap<String, LinkedHashMap<?, String>> legal = this.legal();
			if (legal == null) {
				legal = new LinkedHashMap<String, LinkedHashMap<?, String>>();
			}
			Field[] fields = this.getClass().getFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(ApiField.class)) {
					continue;
				}
				
				field.setAccessible(true);
				Class<?> type = field.getType();
				String key = field.getName();
				Object value = field.get(this);
				ApiField verify = field.getAnnotation(ApiField.class);
				String name = verify.name();
				String used = verify.used();
				boolean need = verify.need();
				boolean empty1 = verify.empty();
				if (value == null || value.toString().equals("")
					|| (type.isArray() || type == java.util.List.class) && format(value, String.class).equals("[]")
					|| format(value, String.class).equals("{}")) {
					if (!used.equals("")) {
						try {
							Object temp = SHARE$SACK.get(used);
							if (temp != null) {
								if (type.isArray()) {
									Class<?> next = type.getComponentType();
									if (next == Boolean.class) {
										value = ((java.util.List<Boolean>) temp).toArray(new Boolean[] {});
									} else if (next == Integer.class) {
										value = ((java.util.List<Integer>) temp).toArray(new Integer[] {});
									} else if (next == Long.class) {
										value = ((java.util.List<Long>) temp).toArray(new Long[] {});
									} else if (next == Double.class) {
										value = ((java.util.List<Double>) temp).toArray(new String[] {});
									} else if (next == String.class) {
										value = ((java.util.List<String>) temp).toArray(new String[] {});
									} else {
										res.setState(ApiResponseState.BUSINESS_PRESENT_NONSUPPORT, name + "(" + key + ")暂不处理类型");
										return true;
									}
									field.set(this, value);
								} else {
									if (type == Boolean.class) {
										value = (Boolean) temp;
									} else if (type == Integer.class) {
										value = (Integer) temp;
									} else if (type == Long.class) {
										value = (Long) temp;
									} else if (type == Double.class) {
										value = (Double) temp;
									} else if (type == String.class) {
										value = (String) temp;
									} else if (type == java.util.List.class) {
										value = (java.util.List<?>) temp;
									} else if (type == java.util.Map.class) {
										value = (java.util.Map<?, ?>) temp;
									} else {
										res.setState(ApiResponseState.BUSINESS_PRESENT_NONSUPPORT, name + "(" + key + ")暂不处理类型");
										return true;
									}
									field.set(this, value);
								}
								SHARE$SACK.remove(used);
							}
						} catch (Exception e) {
							e.printStackTrace();
							res.setState(ApiResponseState.PARAMETER_TYPE_ILLEGAL, name + "(" + key + ")参数类型非法");
							return true;
						}
					}
					
					if (value == null || value.toString().equals("")
						|| (type.isArray() || type == java.util.List.class) && format(value, String.class).equals("[]")
						|| type == java.util.Map.class && format(value, String.class).equals("{}")) {
						if (need) {
							res.setState(ApiResponseState.REQUEST_PARAMETER_EMPTY, name + "(" + key + ")不能设置为空");
							return true;
						}
						if (empty1) {
							field.set(this, null);
						}
						continue;
					}
				}
				
				if (type.isArray()) {
					if (!nozzle(type, false, false, false)) {
						res.setState(ApiResponseState.BUSINESS_PRESENT_NONSUPPORT, name + "(" + key + ")暂不处理类型");
						return true;
					}
					if (verify.length() > 0 && format(value, String.class).length() > verify.length()) {
						res.setState(ApiResponseState.PARAMETER_LENGTH_EXCEED, verify.name() + "(" + key + ")超过最大长度");
						return true;
					}
					Object[] block = (Object[]) value;
					if (verify.block() > 0
						&& block.length > verify.block()) {
						res.setState(ApiResponseState.ARRAY_LENGTH_EXCEED, name + "(" + key + ")超过最大个数");
						return true;
					}
					
					Class<?> next = type.getComponentType();
					ArrayList<Object> center = new ArrayList<Object>();
					for (Object item : block) {
						Object temp = this.verify(next, key, item, verify, legal, res);
						if (res.getState() > 0) {
							return true;
						}
						if (temp != null) {
							center.add(temp);
						}
					}
					
					Object[] data = null;
					if (next == Boolean.class) {
						data = center.toArray(new Boolean[] {});
					} else if (next == Integer.class) {
						data = center.toArray(new Integer[] {});
					} else if (next == Long.class) {
						data = center.toArray(new Long[] {});
					} else if (next == Double.class) {
						data = center.toArray(new Double[] {});
					} else if (next == String.class) {
						data = center.toArray(new String[] {});
					}
					
					if (center.size() > 0) {
						field.set(this, data);
					} else {
						if (need) {
							res.setState(ApiResponseState.REQUEST_PARAMETER_EMPTY, name + "(" + key + ")不能设置为空");
							return true;
						}
						field.set(this, empty1 ? null : data);
					}
				} else if (nozzle(type, false, false, false)) {
					if (verify.length() > 0 && format(value, String.class).length() > verify.length() + (type == String.class ? 2 : 0)) {
						res.setState(ApiResponseState.PARAMETER_LENGTH_EXCEED, verify.name() + "(" + key + ")超过最大长度");
						return true;
					}
					if (this.verify(type, key, value, verify, legal, res) == null) {
						return true;
					}
				} else if (type == java.util.List.class || type == java.util.Map.class) {
					if (verify.length() > 0 && format(value, String.class).length() > verify.length()) {
						res.setState(ApiResponseState.PARAMETER_LENGTH_EXCEED, verify.name() + "(" + key + ")超过最大长度");
						return true;
					}
				} else {
					res.setState(ApiResponseState.BUSINESS_PRESENT_NONSUPPORT, name + "(" + key + ")暂不处理类型");
					return true;
				}
			}
			
			ArrayList<String> field = new ArrayList<String>();
			if (SHARE$FIELD != null && SHARE$FIELD.length > 0) {
				LinkedHashMap<String, String> FIELD = this.field();
				if (FIELD != null && FIELD.size() > 0) {
					for (String key : SHARE$FIELD) {
						if (!FIELD.containsKey(key)) {
							res.setState(ApiResponseState.ARRAY_VALUE_ILLEGAL, "查询字段(SHARE$FIELD)参数取值非法");
							return true;
						}
						if (!field.contains(key)) {
							field.add(key);
						}
					}
				}
			}
			SHARE$FIELD = field.size() > 0 ? field.toArray(new String[] {}) : null;
			
			if (SHARE$QUEUE == null || this.order() == null || !this.order().containsKey(SHARE$ORDER)) {
				SHARE$ORDER = null;
				SHARE$QUEUE = null;
			}
			
			if (this.leaf() == ApiLeafer.NUMBER_LEAFER) {
				if (SHARE$SIZE == null || SHARE$SIZE < 1) {
					SHARE$SIZE = null;
				}
				SHARE$NOW = null;
				SHARE$MARKER = null;
				SHARE$KEYS = null;
			} else if (this.leaf() == ApiLeafer.GENERAL_LEAFER) {
				if (SHARE$SIZE == null || SHARE$SIZE < 1) {
					SHARE$SIZE = null;
				}
				if (SHARE$NOW == null || SHARE$NOW < 1) {
					SHARE$NOW = null;
				}
				SHARE$MARKER = null;
				SHARE$KEYS = null;
			} else if (this.leaf() == ApiLeafer.MARKER_LEAFER) {
				if (SHARE$SIZE == null || SHARE$SIZE < 1) {
					SHARE$SIZE = null;
				}
				SHARE$NOW = null;
				if (SHARE$MARKER == null || SHARE$MARKER.equals("")) {
					SHARE$MARKER = null;
				}
				if (SHARE$KEYS == null || SHARE$KEYS.length < 1) {
					SHARE$KEYS = null;
				}
			} else {
				SHARE$SIZE = null;
				SHARE$NOW = null;
				SHARE$MARKER = null;
				SHARE$KEYS = null;
			}
		} catch (Exception e) {
			res.setState(e);
		}
		return false;
	}
	/**
	 * 检校主体参数
	 * @param request 请求对象
	 * @param res 响应数据
	 * @category 当验证失败时返回true
	 * */
	public boolean verify(HttpServletRequest request, ApiResponse res) {
		if (request == null || res == null) {
			return true;
		}
		try {
			Class<? extends ApiRequest> clazz = this.getClass();
			ConcurrentHashMap<String, String[]> param = new ConcurrentHashMap<String, String[]>();
			param.putAll(request.getParameterMap());
			for (java.util.Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				String key = en.getKey();
				ArrayList<String> value = new ArrayList<String>(Arrays.asList(en.getValue()));
				value.removeAll(Arrays.asList("", null));
				if (value.size() < 1) {
					continue;
				}
				
				if ("SHARE$ARGS".equals(key) || "SHARE$QUEUE".equals(key)) {
					Field field = clazz.getField(key);
					field.setAccessible(true);
					field.set(this, Boolean.parseBoolean(value.get(0)));
					param.remove(key);
				} else if ("SHARE$SIZE".equals(key) || "SHARE$NOW".equals(key)) {
					Field field = clazz.getField(key);
					field.setAccessible(true);
					field.set(this, Long.parseLong(value.get(0)));
					param.remove(key);
				} else if ("SHARE$ORDER".equals(key) || "SHARE$MARKER".equals(key)) {
					Field field = clazz.getField(key);
					field.setAccessible(true);
					field.set(this, value.get(0));
					param.remove(key);
				} else if ("SHARE$FIELD".equals(key) || "SHARE$FIELD[]".equals(key) || "SHARE$KEYS".equals(key)|| "SHARE$KEYS[]".equals(key)) {
					Field field = clazz.getField(key.replace("[]", ""));
					field.setAccessible(true);
					field.set(this, value.toArray(new String[] {}));
					param.remove(key);
				}
			}
			
			for (java.util.Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				String key = en.getKey();
				Field field;
				try {
					field = clazz.getDeclaredField(key);
				} catch (Exception e) {
					continue;
				}
				
				ArrayList<String> value = new ArrayList<String>(Arrays.asList(en.getValue()));
				value.removeAll(Arrays.asList("", null));
				if (value.size() < 1) {
					continue;
				}
				
				field.setAccessible(true);
				Class<?> type = field.getType();
				if (type.isArray()) {
					Class<?> next = type.getComponentType();
					if (next == Boolean.class) {
						field.set(this, format(format(value, String.class), Boolean[].class));
						param.remove(key);
					} else if (next == Integer.class) {
						field.set(this, format(format(value, String.class), Integer[].class));
						param.remove(key);
					} else if (next == Long.class) {
						field.set(this, format(format(value, String.class), Long[].class));
						param.remove(key);
					} else if (next == Double.class) {
						field.set(this, format(format(value, String.class), Double[].class));
						param.remove(key);
					} else if (next == String.class) {
						field.set(this, value.toArray(new String[] {}));
						param.remove(key);
					}
				} else {
					if (type == Boolean.class) {
						field.set(this, Boolean.parseBoolean(value.get(0)));
						param.remove(key);
					} else if (type == Integer.class) {
						field.set(this, Integer.parseInt(value.get(0)));
						param.remove(key);
					} else if (type == Long.class) {
						field.set(this, Long.parseLong(value.get(0)));
						param.remove(key);
					} else if (type == Double.class) {
						field.set(this, Double.parseDouble(value.get(0)));
						param.remove(key);
					} else if (type == String.class) {
						field.set(this, value.get(0));
						param.remove(key);
					} else {
						System.err.println("不提供其他类型的数据手动注入");
					}
				}
			}
			SHARE$SACK.putAll(param);
			return this.verify(res);
		} catch (Exception e) {
			if (e instanceof NumberFormatException
				|| e instanceof InvalidFormatException) {
				res.setState(ApiResponseState.PARAMETER_TYPE_ILLEGAL, "参数类型非法");
				return true;
			}
			res.setState(e);
			return true;
		}
	}
	/**
	 * 设置取值集合
	 * */
	public abstract LinkedHashMap<String, LinkedHashMap<?, String>> legal();
	/**
	 * 可选查询字段
	 * @category 对于查询类接口，可指定调用接口需返回的字段集（表结构中的列）
	 * */
	public abstract LinkedHashMap<String, String> field();
	/**
	 * 设置排序字段
	 * @category 推荐使用排序字段中的第一个键
	 * */
	public abstract LinkedHashMap<String, String> order();
	/**
	 * 设置分页模式
	 * */
	public abstract ApiLeafer leaf();
	/**
	 * 设置响应格式
	 * */
	public abstract LinkedHashMap<String, Object> body();
	/**
	 * 集合类型检测
	 * @param field 字段对象
	 * */
	private static ArrayList<Class<?>> nozzle(Field field) throws Exception {
		ArrayList<Class<?>> res = new ArrayList<Class<?>>();
		if (field.getType() == java.util.List.class) {
			Type generic = field.getGenericType();
			if(generic == null || !(generic instanceof ParameterizedType)) {
				return null;
			}
			
			Type[] type = ((ParameterizedType) generic).getActualTypeArguments();
			res.add(type[0].getTypeName().equals("?") ? null : Class.forName(type[0].getTypeName()));
		} else if (field.getType() == java.util.Map.class) {
			Type generic = field.getGenericType();
			if(generic == null || !(generic instanceof ParameterizedType)) {
				return null;
			}
			
			Type[] type = ((ParameterizedType) generic).getActualTypeArguments();
			res.add(type[0].getTypeName().equals("?") ? null : Class.forName(type[0].getTypeName()));
			res.add(type[1].getTypeName().equals("?") ? null : Class.forName(type[1].getTypeName()));
		}
		return res;
	}
	/**
	 * 判断数据类型
	 * @param type 数据类型
	 * @param number 仅数字吗
	 * @param primitive 要原始吗
	 * @param gather 要集合吗
	 * */
	private static boolean nozzle(Class<?> type, boolean number, boolean primitive, boolean gather) {
		if (type.isArray()) {
			Class<?> next = type.getComponentType();
			if (number) {
				if (primitive) {
					if (next == Integer.class || next == int.class
						|| next == Long.class || next == long.class
						|| next == Double.class || next == double.class) {
						return true;
					}
				} else {
					if (next == Integer.class
						|| next == Long.class
						|| next == Double.class) {
						return true;
					}
				}
			} else {
				if (primitive) {
					if (next == Boolean.class || next == boolean.class
						|| next == Integer.class || next == int.class
						|| next == Long.class || next == long.class
						|| next == Double.class || next == double.class
						|| next == String.class) {
						return true;
					}
				} else {
					if (next == Boolean.class
						|| next == Integer.class
						|| next == Long.class
						|| next == Double.class
						|| next == String.class) {
						return true;
					}
				}
			}
		} else {
			if (number) {
				if (primitive) {
					if (type == Integer.class || type == int.class
						|| type == Long.class || type == long.class
						|| type == Double.class || type == double.class) {
						return true;
					}
				} else {
					if (type == Integer.class
						|| type == Long.class
						|| type == Double.class) {
						return true;
					}
				}
			} else {
				if (primitive) {
					if (gather) {
						if (type == Boolean.class || type == boolean.class
							|| type == Integer.class || type == int.class
							|| type == Long.class || type == long.class
							|| type == Double.class || type == double.class
							|| type == String.class
							|| type == java.util.List.class
							|| type == java.util.Map.class) {
							return true;
						}
					} else {
						if (type == Boolean.class || type == boolean.class
							|| type == Integer.class || type == int.class
							|| type == Long.class || type == long.class
							|| type == Double.class || type == double.class
							|| type == String.class) {
							return true;
						}
					}
				} else {
					if (gather) {
						if (type == Boolean.class
							|| type == Integer.class
							|| type == Long.class
							|| type == Double.class
							|| type == String.class
							|| type == java.util.List.class
							|| type == java.util.Map.class) {
							return true;
						}
					} else {
						if (type == Boolean.class
							|| type == Integer.class
							|| type == Long.class
							|| type == Double.class
							|| type == String.class) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * 查找字节文件
	 * @param packet 文件包名
	 * @param root 文件对象
	 * @param res 返回集合
	 * */
	private static void nozzle(String packet, File root, ArrayList<Class<?>> res) throws Exception {
		if (root == null || !root.exists() || !root.isDirectory()) {
			return;
		}
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(root.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".class");
			}
		})));
		Collections.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				if (o1.isAbsolute() && o2.isFile()) {
					return 1;
				} else if (o1.isFile() && o2.isDirectory()) {
					return -1;
				}
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (File file : files) {
			if (file.isDirectory()) {
				nozzle(packet + "." + file.getName(), file, res);
			} else {
				res.add(Class.forName(packet + "." + file.getName().replace(".class", "")));
			}
		}
	}
	/**
	 * 计算接口编号
	 * @param clazz 对象标志
	 * @param method 接口标志
	 * */
	private static int nozzle(int clazz, int method) throws Exception {
		if (ApiConfig.PROJECT$MARK == null
			|| ApiConfig.PROJECT$MARK % 1001000000 != 0
			|| clazz == 0
			|| method == 0) {
			return 0;
		}
		return ApiConfig.PROJECT$MARK + clazz * 1000 + method;
	}
	/**
	 * 解析接口字段
	 * @param that 对象类型
	 * @param res 返回对象
	 * */
	private static void nozzle(Class<?> that, Result.ApiMethod res) throws Exception {
		Field[] fields = that.getDeclaredFields();
		for (Field field : fields) {
			if (ApiConfig.SHARE$FORBID.contains(field.getName().toUpperCase())) {
				throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")父类有此字段");
			}
			
			int modifiers = field.getModifiers();
			if (!Modifier.isPublic(modifiers)
				|| Modifier.isAbstract(modifiers)
				|| Modifier.isFinal(modifiers)
				|| Modifier.isNative(modifiers)
				|| Modifier.isStatic(modifiers)
				|| Modifier.isStrict(modifiers)
				|| Modifier.isSynchronized(modifiers)
				|| Modifier.isTransient(modifiers)
				|| Modifier.isVolatile(modifiers)) {
				throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")数据类型非法");
			}
			
			Class<?> clazz = field.getType();
			if (!nozzle(clazz, false, false, true)) {
				throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")数据类型非法");
 			}
			
			String[] generic = new String[] { "", "" };
			if (clazz == java.util.List.class || clazz == java.util.Map.class) {
				ArrayList<Class<?>> temp = nozzle(field);
				if (temp == null || temp.size() < 1) {
					throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")数据类型非法");
				}
				
				for (Class<?> clazz2 : temp) {
					if (clazz2 == null || clazz2 != Object.class && !nozzle(clazz2, false, false, false)) {
						throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")数据类型非法");
					}
					generic[0] += ", " + clazz2.getName();
					generic[1] += ", " + clazz2.getSimpleName();
				}
				generic[0] = "<" + generic[0].substring(2) + ">";
				generic[1] = "<" + generic[1].substring(2) + ">";
			}
			
			ArrayList<String[]> nozzle = new ArrayList<String[]>();
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				nozzle.add(new String[] { annotation.annotationType().getName(), annotation.annotationType().getSimpleName() });
			}
			
			if (field.isAnnotationPresent(ApiField.class)) {
				if (field.getAnnotations().length > 1) {
					throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")注解使用非法");
				}
				
				ApiField verify = field.getAnnotation(ApiField.class);
				Result.ApiField data = new Result.ApiField();
				data.name = verify.name();
				if (data.name.equals("")) {
					throw new Exception("[" + that.getName() + "]参数解析错误->(" + field.getName() + ")为空");
				}
				if (data.name.length() > 20) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")参数名称过长");
				}
				if (verify.need() && !verify.empty()) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")必填必须置空");
				}
				if (verify.length() < 0) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")最大长度非法");
				}
				if (verify.length() > 0 && clazz == Boolean.class) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")最大长度非法");
				}
				if (verify.block() < 0) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")数组长度非法");
				}
				if (verify.block() > 0 && !clazz.isArray()) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")数组长度非法");
				}
				if (verify.smaller() != Long.MIN_VALUE || verify.bigger() != Long.MAX_VALUE) {
					if (clazz.isArray() || !nozzle(clazz, true, false, false)) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")设值范围非法");
					}
					if (verify.smaller() >= verify.bigger()) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")设值范围非法");
					}
					if (!verify.kagi().equals("")) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")设值范围非法");
					}
				}
				LinkedHashMap<?, String> kagi = null;
				if (!verify.kagi().equals("") && (kagi = res.legal.get(verify.kagi())) == null) {
					throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")取值标识非法");
				}
				if (kagi != null) {
					if (verify.length() > 0) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")取值标识非法");
					}
					if (!clazz.isArray() && (clazz == Boolean.class || !nozzle(clazz, false, false, false))) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")取值标识非法");
					}
				}
				
				data.field = field.getName();
				data.used = verify.used().equals("") ? null : verify.used();
				data.nozzle = nozzle;
				data.type = new String[2];
				if (clazz.isArray()) {
					data.type[0] = clazz.getCanonicalName();
					data.type[1] = clazz.getSimpleName();
				} else {
					data.type[0] = clazz.getName() + generic[0];
					data.type[1] = clazz.getSimpleName() + generic[1];
				}
				data.need = verify.need();
				data.length = verify.length() > 0 ? verify.length() : null;
				data.block = verify.block() > 0 ? verify.block() : null;
				if (clazz.isArray() && kagi != null) {
					if (data.block == null) {
						data.block = kagi.size();
					}else if (data.block > kagi.size()) {
						throw new Exception("[" + that.getName() + "]参数解析错误->" + data.name + "(" + field.getName() + ")数组长度非法");
					}
				}
				data.smaller = verify.smaller() != Long.MIN_VALUE ? verify.smaller() : null;
				data.bigger = verify.bigger() != Long.MAX_VALUE ? verify.bigger() : null;
				data.kagi = verify.kagi().equals("") ? null : verify.kagi();
				data.layout = verify.layout().equals("") ? null : verify.layout();
				data.note = verify.note().equals("") ? null : verify.note();
				res.next.add(data);
			} else {
				Result.ApiField data = new Result.ApiField();
				data.field = field.getName();
				data.type = new String[2];
				if (clazz.isArray()) {
					data.type[0] = clazz.getCanonicalName();
					data.type[1] = clazz.getSimpleName();
				} else {
					data.type[0] = clazz.getName() + generic[0];
					data.type[1] = clazz.getSimpleName() + generic[1];
				}
				data.nozzle = nozzle;
				if (that.isAnnotationPresent(Validated.class) && ApiConfig.DOC$VERIFTY != null) {
					ApiConfig.DOC$VERIFTY.verifty(field, data);
				}
				res.next.add(data);
			}
		}
	}
	/**
	 * 生成接口文档
	 * @param clazz 对象类型
	 * @see <a href="https://www.cnblogs.com/weiyinfu/p/8339825.html">https://www.cnblogs.com/weiyinfu/p/8339825.html</a>
	 * */
	private static ArrayList<Result.ApiMethod> nozzle(Class<?> clazz) throws Exception {
		ArrayList<Result.ApiMethod> res = new ArrayList<Result.ApiMethod>();
		Annotation[] annotations = clazz.getAnnotations();
		Integer MARK = null;
		String LUMP = "";
		String TITLE = "";
		String NAME = "";
		String[] MAPPER = null;
		String[] PATH = new String[0];
		String[] HEADERS = new String[0];
		String[] CONSUMES = new String[0];
		String[] PRODUCES = new String[0];
		String[] PARAMS = new String[0];
		RequestMethod[] METHOD = new RequestMethod[0];
		for (Annotation annotation : annotations) {
			if (annotation instanceof ApiClass) {
				ApiClass mapper = (ApiClass) annotation;
				MARK = mapper.mark();
				LUMP = mapper.lump();
				TITLE = mapper.title();
				if (MARK < 0 || MARK > 999) {
					throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求对象(" + clazz.getName() + ")对象标志非法");
				}
				if (TITLE.equals("")) {
					throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求对象(" + clazz.getName() + ")对象名称为空");
				}
			} else if (annotation instanceof RequestMapping) {
				RequestMapping mapper = (RequestMapping) annotation;
				NAME = mapper.name();
				MAPPER = new String[] { RequestMapping.class.getName(), RequestMapping.class.getSimpleName() };
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = mapper.method();
			} else if (annotation instanceof GetMapping) {
				GetMapping mapper = (GetMapping) annotation;
				NAME = mapper.name();
				MAPPER = new String[] { GetMapping.class.getName(), GetMapping.class.getSimpleName() };
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = new RequestMethod[] { RequestMethod.GET };
			} else if (annotation instanceof PostMapping) {
				PostMapping mapper = (PostMapping) annotation;
				MAPPER = new String[] { PostMapping.class.getName(), PostMapping.class.getSimpleName()};
				NAME = mapper.name();
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = new RequestMethod[] { RequestMethod.POST };
			} else if (annotation instanceof PutMapping) {
				PutMapping mapper = (PutMapping) annotation;
				MAPPER = new String[] { PutMapping.class.getName(), PutMapping.class.getSimpleName() };
				NAME = mapper.name();
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = new RequestMethod[] { RequestMethod.PUT };
			} else if (annotation instanceof DeleteMapping) {
				DeleteMapping mapper = (DeleteMapping) annotation;
				MAPPER = new String[] { DeleteMapping.class.getName(), DeleteMapping.class.getSimpleName() };
				NAME = mapper.name();
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = new RequestMethod[] { RequestMethod.DELETE };
			} else if (annotation instanceof PatchMapping) {
				PatchMapping mapper = (PatchMapping) annotation;
				MAPPER = new String[] { PatchMapping.class.getName(), PatchMapping.class.getSimpleName() };
				NAME = mapper.name();
				PATH = mapper.value().length > 0 ? mapper.value() : mapper.path();
				HEADERS = mapper.headers();
				CONSUMES = mapper.consumes();
				PRODUCES = mapper.produces();
				PARAMS = mapper.params();
				METHOD = new RequestMethod[] { RequestMethod.PATCH };
			}
		}
		
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(ApiRequestMethod.class)) {
				continue;
			}
			
			String name = "";
			annotations = method.getAnnotations();
			Result.ApiMethod data = new Result.ApiMethod();
			for (Annotation annotation : annotations) {
				if (annotation instanceof ApiRequestMethod) {
					ApiRequestMethod verify = (ApiRequestMethod) annotation;
					if (verify.mark() < 0 || verify.mark() > 999) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")接口标志非法");
					}
					if (verify.name().equals("")) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")接口标题为空");
					}
					if (verify.queue() < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")排列序号非法");
					}
					
					data.id = nozzle(MARK, verify.mark());
					data.name = verify.name();
					data.used = new ArrayList<String>(Arrays.asList(verify.used()));
					data.used.removeAll(Arrays.asList(""));
					data.token = verify.token();
					data.journal = verify.journal();
					data.consume = verify.consume().equals("") ? null : verify.consume();
					data.produce = verify.produce().equals("") ? null : verify.produce();
					data.brief = verify.brief().equals("") ? null : verify.brief();
					data.jump = new ArrayList<String>(Arrays.asList(verify.jump()));
					data.jump.removeAll(Arrays.asList(""));
					data.queue = verify.queue();
				} else if (annotation instanceof RequestMapping) {
					data.mapper = new String[][]{ MAPPER, { RequestMapping.class.getName(), RequestMapping.class.getSimpleName() } };
					RequestMapping mapper = (RequestMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					RequestMethod[] next = mapper.method().length > 0 ? mapper.method() : METHOD;
					data.mode = new ArrayList<String>();
					if (next.length > 0) {
						for (int i = 0; i < next.length; i++) {
							data.mode.add(next[i].name());
						}
					}
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				} else if (annotation instanceof GetMapping) {
					data.mapper = new String[][] { MAPPER, { GetMapping.class.getName(), GetMapping.class.getSimpleName() } };
					GetMapping mapper = (GetMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					data.mode = Arrays.asList(HttpMethod.GET.name());
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				} else if (annotation instanceof PostMapping) {
					data.mapper = new String[][] { MAPPER, { PostMapping.class.getName(), PostMapping.class.getSimpleName() } };
					PostMapping mapper = (PostMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					data.mode = Arrays.asList(HttpMethod.POST.name());
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				} else if (annotation instanceof PutMapping) {
					data.mapper = new String[][] { MAPPER, { PutMapping.class.getName(), PutMapping.class.getSimpleName() } };
					PutMapping mapper = (PutMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					data.mode = Arrays.asList(HttpMethod.PUT.name());
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				} else if (annotation instanceof DeleteMapping) {
					data.mapper = new String[][] { MAPPER, { DeleteMapping.class.getName(), DeleteMapping.class.getSimpleName() } };
					DeleteMapping mapper = (DeleteMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					data.mode = Arrays.asList(HttpMethod.DELETE.name());
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				} else if (annotation instanceof PatchMapping) {
					data.mapper = new String[][] { MAPPER, { PatchMapping.class.getName(), PatchMapping.class.getSimpleName() } };
					PatchMapping mapper = (PatchMapping) annotation;
					name = mapper.name();
					String[] path = mapper.value().length > 0 ? mapper.value() : mapper.path();
					if (path.length < 1 && PATH.length > 0) {
						path = PATH;
						PATH = new String[0];
					}
					if (path.length < 1) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")请求地址非法");
					}
					
					data.path = new ArrayList<String>();
					if (PATH.length > 0) {
						for (String prefix : PATH) {
							for (String next : path) {
								data.path.add(prefix + (!prefix.equals("") && !prefix.endsWith("/") && !next.startsWith("/") ? "/" : "") + next);
							}
						}
					} else {
						data.path.addAll(Arrays.asList(path));
					}
					
					data.mode = Arrays.asList(HttpMethod.PATCH.name());
					
					data.headers = new String[2][];
					data.headers[0] = HEADERS;
					data.headers[1] = mapper.headers();
					
					data.consumes = new String[2][];
					data.consumes[0] = CONSUMES;
					data.consumes[1] = mapper.consumes();
					
					data.produces = new String[2][];
					data.produces[0] = PRODUCES;
					data.produces[1] = mapper.produces();
					
					data.params = new String[2][];
					data.params[0] = PARAMS;
					data.params[1] = mapper.params();
				}
			}
			if (data.mapper == null) {
				throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")不是合法接口");
			}
			
			Parameter[] parameters = method.getParameters();
			Class<?>[] param = new Class<?>[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				param[i] = parameters[i].getType();
			}
			String[] local = new LocalVariableTableParameterNameDiscoverer().getParameterNames(clazz.getDeclaredMethod(method.getName(), param));
			
			Class<?> revert = method.getReturnType();
			boolean inherit = Arrays.asList(revert.getInterfaces()).contains(ApiResponse.class);
			StringBuffer state = new StringBuffer(nozzle(method.getModifiers()) + revert.getName() + " " + method.getName() + "(");
			data.attach = new ArrayList<Result.ApiField>();
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				Class<?> type = parameter.getType();
				PathVariable pathVariable = null;
				RequestParam requestParam = null;
				RequestBody requestBody = null;
				ArrayList<String[]> nozzle = new ArrayList<String[]>();
				for (Annotation annotation : parameter.getAnnotations()) {
					if (annotation.annotationType() == PathVariable.class) {
						if (!nozzle(type, false, true, false)) {
							throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")注解用法错误");
						}
						pathVariable = (PathVariable) annotation;
					} else if (annotation.annotationType() == RequestParam.class) {
						if (!nozzle(type, false, true, false)) {
							throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")注解用法错误");
						}
						requestParam = (RequestParam) annotation;
					} else if (annotation.annotationType() == RequestBody.class) {
						if (requestBody != null) {
							throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")注解用法错误");
						}
						requestBody = (RequestBody) annotation;
					}
					nozzle.add(new String[] { annotation.annotationType().getName(), annotation.annotationType().getSimpleName() });
					state.append("@" + annotation.annotationType().getName() + " ");
				}
				state.append(parameter.getParameterizedType().getTypeName() + " " + local[i] + ", ");
				
				if (pathVariable != null && requestParam != null
					|| pathVariable != null && requestBody != null
					|| requestParam != null && requestBody != null) {
					throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")注解用法错误");
				}
				if (type.getSuperclass() == ApiRequest.class) {
					if (pathVariable != null || requestParam != null) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")注解用法错误");
					}
					if (data.next != null) {
						throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")主体参数重复");
					}
					
					ApiRequest next = (ApiRequest) type.newInstance();
					data.entry = new String[] { type.getName(), type.getSimpleName() };
					int pot = data.entry[0].lastIndexOf(".");
					if (pot != -1) {
						data.entry[1] = data.entry[0].substring(pot + 1);
					}
					data.nozzle = nozzle;
					data.next = new ArrayList<Result.ApiField>();
					data.legal = next.legal() != null ? next.legal() : new LinkedHashMap<String, LinkedHashMap<?, String>>();
					
					data.share = new ArrayList<Result.ApiField>();
					Result.ApiField temp = new Result.ApiField("返参数吗", "SHARE$ARGS", new String[] { Boolean.class.getName(), Boolean.class.getSimpleName() });
					temp.note = "默认不返回请求参数";
					data.share.add(temp);
					if (next.field() != null && next.field().size() > 0) {
						temp = new Result.ApiField("查询字段", "SHARE$FIELD", new String[] { String[].class.getCanonicalName(), String[].class.getSimpleName() });
						temp.block = next.field().size();
						temp.kagi = "SHARE$FIELD";
						data.legal.put("SHARE$FIELD", next.field());
						data.share.add(temp);
					}
					if (next.order() != null && next.order().size() > 0) {
						temp = new Result.ApiField("排序字段", "SHARE$ORDER", new String[] { String.class.getName(), String.class.getSimpleName() });
						temp.kagi = "SHARE$FIELD";
						data.legal.put("SHARE$FIELD", next.order());
						data.share.add(temp);
						temp = new Result.ApiField("是否升序", "SHARE$QUEUE", new String[] { Boolean.class.getName(), Boolean.class.getSimpleName() });
						temp.note = "与排序字段同时使用";
						data.share.add(temp);
					}
					if (next.leaf() == ApiLeafer.NUMBER_LEAFER) {
						temp = new Result.ApiField("当前页号", "SHARE$NOW", new String[] { Long.class.getName(), Long.class.getSimpleName() });
						temp.smaller = 1L;
						data.share.add(temp);
						if (inherit) {
							ApiResponse body = (ApiResponse) revert.newInstance();
							body.setBody(next.body() != null ? next.body() : new LinkedHashMap<String, Object>());
							body.setPage(ApiLeafer.GENERAL_LEAFER, 0L, 5L, 0L, 1L, null);
							data.body = body.setState();
						}
					} else if (next.leaf() == ApiLeafer.GENERAL_LEAFER) {
						temp = new Result.ApiField("每页条数", "SHARE$SIZE", new String[] { Long.class.getName(), Long.class.getSimpleName() });
						temp.smaller = 1L;
						temp.bigger = ApiConfig.SHARE$SIZE_MAX;
						data.share.add(temp);
						temp = new Result.ApiField("当前页号", "SHARE$NOW", new String[] { Long.class.getName(), Long.class.getSimpleName() });
						temp.smaller = 1L;
						data.share.add(temp);
						if (inherit) {
							ApiResponse body = (ApiResponse) revert.newInstance();
							body.setBody(next.body() != null ? next.body() : new LinkedHashMap<String, Object>());
							body.setPage(ApiLeafer.GENERAL_LEAFER, 0L, 5L, 0L, 1L, null);
							data.body = body.setState();
						}
					} else if (next.leaf() == ApiLeafer.MARKER_LEAFER) {
						temp = new Result.ApiField("每页条数", "SHARE$SIZE", new String[] { Long.class.getName(), Long.class.getSimpleName() });
						temp.smaller = 1L;
						temp.bigger = ApiConfig.SHARE$SIZE_MAX;
						data.share.add(temp);
						data.share.add(new Result.ApiField("分页标识", "SHARE$MARKER", new String[] { String.class.getName(), String.class.getSimpleName() }));
						temp = new Result.ApiField("忽略标识", "SHARE$KEYS", new String[] { String[].class.getCanonicalName(), String[].class.getSimpleName() });
						temp.note = "当使用分隔符分页请求下页时不再返回数据的标识";
						data.share.add(temp);
						if (inherit) {
							ApiResponse body = (ApiResponse) revert.newInstance();
							body.setBody(next.body() != null ? next.body() : new LinkedHashMap<String, Object>());
							body.setPage(ApiLeafer.MARKER_LEAFER, null, 5L, null, null, "分页标识");
							data.body = body.setState();
						}
					}
					if (data.body == null && !ApiConfig.SHARE$REVERT.contains(revert)) {
						data.body = inherit ? ((ApiResponse) revert.newInstance()).setState().setBody(next.body() != null ? next.body() : new LinkedHashMap<String, Object>()) : revert.newInstance();
					}
					nozzle(type, data);
				} else {
					if (type == HttpServletRequest.class
						|| type == HttpServletResponse.class
						|| type == HttpSession.class) {
						continue;
					}
					
					data.legal = new LinkedHashMap<String, LinkedHashMap<?, String>>();
					data.share = new ArrayList<Result.ApiField>();
					if (!ApiConfig.SHARE$REVERT.contains(revert)) {
						data.body = inherit ? ((ApiResponse) revert.newInstance()).setState() : revert.newInstance();
					}
					Result.ApiField field = new Result.ApiField();
					if (pathVariable != null) {
						field.field = pathVariable.value().equals("") ? (pathVariable.name().equals("") ? local[i] : pathVariable.name()) : pathVariable.value();
						field.need = pathVariable.required();
					} else if (requestParam != null) {
						field.field = requestParam.value().equals("") ? (requestParam.name().equals("") ? local[i] : requestParam.name()) : requestParam.value();
						field.need = requestParam.required();
						field.note = requestParam.defaultValue().equals("") || requestParam.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? null : "默认为" + requestParam.defaultValue();
					} else if (requestBody != null) {
						field.field = local[i];
						field.need = requestBody.required();
					} else {
						field.field = local[i];
					}
					
					field.type = new String[2];
					if (type.isArray()) {
						Class<?> item = type.getComponentType();
						if (!nozzle(item, false, true, false)) {
							if (item.getClassLoader() == null) {
								throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")数据类型非法");
							}
							
							//TODO 解析其他参数
						}
						field.type[0] = type.getCanonicalName();
						field.type[1] = type.getSimpleName();
					} else {
						if (nozzle(type, false, true, false)) {
							field.type[0] = type.getName();
							field.type[1] = type.getSimpleName();
						} else if (Arrays.asList(type.getInterfaces()).contains(java.util.List.class)) {
							field.type[0] = parameter.getParameterizedType().getTypeName();
							String item = field.type[0].substring(field.type[0].indexOf("<") + 1, field.type[0].indexOf(">"));
							if (item.equals("?")) {
								throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")数据类型非法");
							}
							
							Class<?> next = Class.forName(item);
							if (next != Object.class && !nozzle(next, false, false, false)) {
								if (next.getClassLoader() == null) {
									throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")数据类型非法");
								}
								
								//TODO 解析其他参数
							}
							field.type[1] = java.util.List.class.getSimpleName() + "<" + (next != null ? next.getSimpleName() : "?") + ">";
						} else if (Arrays.asList(type.getInterfaces()).contains(java.util.Map.class)) {
							field.type[0] = parameter.getParameterizedType().getTypeName();
							String[] item = field.type[0].substring(field.type[0].indexOf("<") + 1, field.type[0].indexOf(">")).split(", ");
							Class<?>[] next = new Class<?>[] { item[0].equals("?") ? null : Class.forName(item[0]), item[1].equals("?") ? null : Class.forName(item[1]) };
							if (next[0] == null || next[1] == null || next[0] != Object.class && !nozzle(next[0], false, false, false) || next[1] != Object.class && !nozzle(next[0], false, false, false)) {
								throw new Exception("[" + clazz.getName() + "]参数解析错误->接口请求方法(" + method.getName() + ")数据类型非法");
							}
							field.type[1] = java.util.Map.class.getSimpleName() + "<" + next[0].getSimpleName() + ", " + next[1].getSimpleName() + ">";
						} else {
							field.type[0] = type.getName();
							field.type[1] = type.getSimpleName();
							if (type.getClassLoader() != null) {
								//TODO 处理其他类型
							}
						}
					}
					field.nozzle = nozzle;
					data.attach.add(field);
				}
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
			data.method = state.toString();
			
			data.lump = LUMP.equals("") ? null : LUMP;
			data.title = TITLE + (NAME.equals("") ? "" : "<" + NAME + ">");
			data.clazz = clazz.getName();
			if (!name.equals("")) {
				data.name += "<" + name + ">";
			}
			if (data.next == null) {
				data.nozzle = new ArrayList<String[]>();
				data.next = new ArrayList<Result.ApiField>();
			}
			res.add(data);
		}
		return res;
	}
	/**
	 * 解析修饰变量
	 * @param modifiers 修饰变量
	 * */
	public static String nozzle(int modifiers) {
		StringBuffer res = new StringBuffer();
		if (Modifier.isPublic(modifiers)) {
			res.append("public ");
		}
		if (Modifier.isProtected(modifiers)) {
			res.append("protected ");
		}
		if (Modifier.isPrivate(modifiers)) {
			res.append("private ");
		}
		if (Modifier.isAbstract(modifiers)) {
			res.append("abstract ");
		}
		if (Modifier.isSynchronized(modifiers)) {
			res.append("synchronized ");
		}
		if (Modifier.isStatic(modifiers)) {
			res.append("static ");
		}
		if (Modifier.isFinal(modifiers)) {
			res.append("final ");
		}
		if (Modifier.isNative(modifiers)) {
			res.append("native ");
		}
		return res.toString();
	}
	/**
	 * 生成全部文档
	 * @param packet 文件包名
	 * */
	public static ArrayList<Result.ApiMethod> nozzle(String packet) throws Exception {
		ArrayList<Class<?>> root = new ArrayList<Class<?>>();
		String front = packet.replace(".", "/");
		URL conn = Thread.currentThread().getContextClassLoader().getResource(front);
		if ("file".equals(conn.getProtocol())) {
			nozzle(packet, new File(URLDecoder.decode(conn.getFile(), "UTF-8")), root);
		} else if ("jar".equals(conn.getProtocol())) {
			JarFile jar = ((JarURLConnection) conn.openConnection()).getJarFile();
			Enumeration<JarEntry> entry = jar.entries();
			while (entry.hasMoreElements()) {
				String name = entry.nextElement().getName();
				if (name.startsWith(front) && name.endsWith(".class")) {
					root.add(Class.forName(name.replace(".class", "").replace("/", ".")));
				}
			}
		}
		
		ArrayList<Result.ApiMethod> res = new ArrayList<Result.ApiMethod>();
		for (Class<?> clazz : root) {
			if (!clazz.isAnnotationPresent(ApiClass.class) || !clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}
			ArrayList<Result.ApiMethod> temp = nozzle(clazz);
			if (temp.size() > 0) {
				res.addAll(temp);
			}
		}
		
		if (ApiConfig.NOZZLE$COMPARATOR != null) {
			Collections.sort(res, ApiConfig.NOZZLE$COMPARATOR);
		} else {
			Collections.sort(res, new Comparator<Result.ApiMethod>() {
				public int compare(Result.ApiMethod o1, Result.ApiMethod o2) {
					if (o1.id > o2.id) {
						return 1;
					} else if (o1.id < o2.id) {
						return - 1;
					} else {
						if (o1.title.equals(o2.title)) {
							if (o1.queue == o2.queue) {
								return 0;
							}
							return o1.queue > o2.queue ? 1 : -1;
						}
						return 0;
					}
					
				}
			});
		}
		return res;
	}
	/**
	 * 数据格式工具
	 * @param data 数据对象
	 * @param clazz 返回类型
	 * */
	@SuppressWarnings("unchecked")
	public static <T> T format(Object data, Class<T> clazz, boolean... indent) throws Exception {
		if (data == null) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		if (indent != null && indent.length > 0 && indent[0]) {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		} else {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
			mapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false);
			mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		}
		if (clazz == String.class) {
			return (T) mapper.writeValueAsString(data);
		} else {
			return mapper.readValue(data.toString(), clazz);
		}
	}
	/**
	 * 分页模式枚举
	 * */
	public enum ApiLeafer {
		NOT_LEAFER("不分页"), NUMBER_LEAFER("仅设页号"), GENERAL_LEAFER("传统分页"), MARKER_LEAFER("分隔符分页");
		/**
		 * 模式名称
		 * */
		public String name;
		/**
		 * 传参构造方法
		 * @param name 模式名称
		 * */
		private ApiLeafer(String name) {
			this.name = name;
		}
	}
	/**
	 * 请求对象注解
	 * */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Documented
	public static @interface ApiClass {
		/**
		 * 对象标志
		 * @category 取值范围为1-999
		 * */
		public int mark() default 0;
		/**
		 * 模块名称
		 * */
		public String lump() default "";
		/**
		 * 对象名称
		 * */
		public String title();
	}
	/**
	 * 请求参数注解
	 * */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	@Documented
	public static @interface ApiField {
		/**
		 * 参数名称
		 * */
		public String name();
		/**
		 * 老版字段
		 * */
		public String used() default "";
		/**
		 * 是否必填
		 * @category 默认为false
		 * */
		public boolean need() default false;
		/**
		 * 空串置空
		 * @category 默认为true
		 * */
		public boolean empty() default true;
		/**
		 * 最大长度
		 * @category 默认为0，必须不小于0，仅限非Boolean及非<b>取值标识</b>类型
		 * */
		public int length() default 0;
		/**
		 * 数组长度
		 * @category 默认为0，必须不小于0，仅限数组类型
		 * */
		public int block() default 0;
		/**
		 * 最小取值
		 * @category 默认为Long.MIN_VALUE，必须不大于<b>最大取值</b>，仅限Integer、Long类型
		 * */
		public long smaller() default Long.MIN_VALUE;
		/**
		 * 最大取值
		 * @category 默认为Long.MAX_VALUE，仅限Integer、Long类型
		 * */
		public long bigger() default Long.MAX_VALUE;
		/**
		 * 取值标识
		 * @category 仅限Integer、Long、String类型，不可设置<b>最大长度</b>
		 * */
		public String kagi() default "";
		/**
		 * 设值举例
		 * */
		public String layout() default "";
		/**
		 * 备注说明
		 * */
		public String note() default "";
	}
	/**
	 * 接口封装对象
	 * */
	public static class Result {
		/**
		 * 请求方法对象
		 * @see <a href="https://www.cnblogs.com/nhdlb/p/11532643.html">https://www.cnblogs.com/nhdlb/p/11532643.html</a>
		 * */
		public static class ApiMethod {
			/**
			 * 唯一编号
			 * */
			public Integer id;
			/**
			 * 模块名称
			 * */
			public String lump;
			/**
			 * 对象名称
			 * */
			public String title;
			/**
			 * 对象类名
			 * */
			public String clazz;
			/**
			 * 接口名称
			 * */
			public String name;
			/**
			 * 方法申明
			 * */
			public String method;
			/**
			 * 方法注解
			 * */
			public String[][] mapper;
			/**
			 * 请求地址
			 * */
			public java.util.List<String> path;
			/**
			 * 请求方式
			 * */
			public java.util.List<String> mode;
			/**
			 * 允许头部
			 * */
			public String[][] headers;
			/**
			 * 请求类型
			 * */
			public String[][] consumes;
			/**
			 * 响应类型
			 * */
			public String[][] produces;
			/**
			 * 允许参数
			 * */
			public String[][] params;
			/**
			 * 老版地址
			 * */
			public java.util.List<String> used;
			/**
			 * 需票据吗
			 * */
			public Boolean token;
			/**
			 * 存日志吗
			 * */
			public Boolean journal;
			/**
			 * 请求类型（默认）
			 * */
			public String consume;
			/**
			 * 响应类型（默认）
			 * */
			public String produce;
			/**
			 * 接口简述
			 * */
			public String brief;
			/**
			 * 相关链接
			 * */
			public java.util.List<String> jump;
			/**
			 * 排列序号
			 * */
			public Integer queue;
			/**
			 * 取值集合
			 * */
			public LinkedHashMap<String, LinkedHashMap<?, String>> legal;
			/**
			 * 通用参数
			 * */
			public java.util.List<ApiField> share;
			/**
			 * 主体类名
			 * */
			public String[] entry;
			/**
			 * 主体注解
			 * */
			public java.util.List<String[]> nozzle;
			/**
			 * 主体参数
			 * */
			public java.util.List<ApiField> next;
			/**
			 * 其他参数
			 * */
			public java.util.List<ApiField> attach;
			/**
			 * 返回数据
			 * */
			public Object body;
			/**
			 * 剔除空字符串
			 * @param data 数组数据
			 * */
			public ArrayList<ArrayList<String>> remove(String[][] data) {
				ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
				ArrayList<String> temp = new ArrayList<String>(Arrays.asList(data[0]));
				temp.removeAll(Arrays.asList(""));
				res.add(temp);
				temp = new ArrayList<String>(Arrays.asList(data[1]));
				temp.removeAll(Arrays.asList(""));
				res.add(temp);
				return res;
			}
		}
		/**
		 * 请求字段对象
		 * */
		public static class ApiField {
			/**
			 * 参数名称
			 * */
			public String name;
			/**
			 * 字段名称
			 * */
			public String field;
			/**
			 * 老版字段
			 * */
			public String used;
			/**
			 * 字段注解
			 * */
			public java.util.List<String[]> nozzle;
			/**
			 * 数据类型
			 * */
			public String[] type;
			/**
			 * 是否必填
			 * */
			public Boolean need;
			/**
			 * 空串置空
			 * */
			public Boolean empty;
			/**
			 * 最大长度
			 * */
			public Integer length;
			/**
			 * 数组长度
			 * */
			public Integer block;
			/**
			 * 最小取值
			 * */
			public Long smaller;
			/**
			 * 最大取值
			 * */
			public Long bigger;
			/**
			 * 设值选项
			 * */
			public String kagi;
			/**
			 * 设值举例
			 * */
			public String layout;
			/**
			 * 备注说明
			 * */
			public String note;
			/**
			 * 匿名构造
			 * */
			public ApiField() {}
			/**
			 * 传参构造方法
			 * @param name 参数名称
			 * @param field 参数字段
			 * @param type 数据类型
			 * */
			public ApiField(String name, String field, String[] type) {
				this.name = name;
				this.field = field;
				this.nozzle = new ArrayList<String[]>();
				this.type = type;
				this.need = false;
			}
		}
	}
}