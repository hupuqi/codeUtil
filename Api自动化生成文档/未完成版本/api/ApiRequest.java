package com.td.api;

import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ApiRequest {

    /**
     * 生成全部api文档
     * @return
     */
    public static ArrayList<ApiResultMethod> nozzle(String packet) throws Exception {
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

        ArrayList<ApiResultMethod> res = new ArrayList<ApiResultMethod>();
        for (Class<?> clazz : root) {
            if (!clazz.isAnnotationPresent(ApiClass.class) || !clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            ArrayList<ApiResultMethod> temp = nozzle(clazz);
            if (temp.size() > 0) {
                res.addAll(temp);
            }
        }

        return res;
    }

    /**
     * 生成接口文档
     * @param clazz
     * @return
     * @throws Exception
     */
    private static ArrayList<ApiResultMethod> nozzle(Class<?> clazz) throws Exception {
        ArrayList<ApiResultMethod> result = new ArrayList<ApiResultMethod>();
        Integer MARK = null;
        String PATH="/";
        String MODULETITLE="";
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ApiClass) {
                ApiClass mapper = (ApiClass) annotation;
                MARK = mapper.mark();
                PATH=mapper.value();
                MODULETITLE=mapper.title();
            }
        }
        Method[] methods = clazz.getMethods();
        for(Method method:methods){
            if (!method.isAnnotationPresent(ApiMethod.class)) {
                continue;
            }
            Parameter[] parameters = method.getParameters();
            //Annotation[][] parameterAnnotations=method.getParameterAnnotations();
            annotations = method.getAnnotations();
            ApiResultMethod data=new ApiResultMethod();
            for(Annotation annotation : annotations){
                if (annotation instanceof ApiMethod) {
                    /************请求方法************/
                    ApiMethod verify = (ApiMethod) annotation;
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
                    data.consume = verify.consume().equals("") ? null : verify.consume();
                    data.produce = verify.produce().equals("") ? null : verify.produce();
                    data.brief = verify.brief().equals("") ? null : verify.brief();
                    data.jump = new ArrayList<String>(Arrays.asList(verify.jump()));
                    data.jump.removeAll(Arrays.asList(""));
                    data.queue = verify.queue();
                    data.value=PATH+verify.value();
                    data.methodMode=verify.methodMode();
                    /*******返回参数********/
                    Class<?> [] returnParamEntity = verify.returnParamEntity();


                }
            }
            /**************请求参数**************/
            List<ApiResultField> fieldList = new ArrayList<ApiResultField>();
            for(Parameter parameter:parameters){
                for(Annotation param:parameter.getAnnotations()){
                    if (param instanceof ApiParam) {
                        ApiParam verify = (ApiParam) param;
                        ApiResultField field =new ApiResultField();
                        field.field = parameter.getName();
                        field.name = verify.name();
                        field.fieldType = parameter.getParameterizedType().getTypeName();
                        field.maxLength = verify.maxLength();
                        field.minLength = verify.minLength();
                        field.note = verify.note();
                        field.need = verify.need();
                        fieldList.add(field);
                    }else if(param instanceof ApiBody){
                        Field[] fields = parameter.getType().getDeclaredFields();
                        for (Field f : fields) {
                            ApiField verify = f.getAnnotation(ApiField.class);
                            if(verify != null){
                                ApiResultField field = new ApiResultField();
                                field.field = f.getName();
                                field.name = verify.name();
                                field.fieldType = parameter.getParameterizedType().getTypeName();
                                field.maxLength = verify.maxLength();
                                field.minLength = verify.minLength();
                                field.need = verify.need();
                                field.note = verify.note();
                                fieldList.add(field);
                            }
                        }
                    }
                }
            }
            data.fieldList = fieldList;
            result.add(data);
        }
        return result;
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
     * 接口对象
     */
    public static class ApiResultMethod{
        /**
         * 唯一编号
         * */
        public Integer id;
        /**
         * 接口标志
         * @category 取值范围为1-999，项目标志 + 对象标志 * 1000 + 接口标志 = 接口唯一编号，该编号与接口测试代码相关联，一旦设置了最好不要修改
         * */
        public int mark;
        /**
         * 接口标题
         * */
        public String name;
        /**
         * 老版地址
         * */
        public java.util.List<String> used;
        /**
         * 需票据吗
         * @category 默认为true
         * */
        public boolean token;

        /**
         * 请求类型
         * @see {@link org.springframework.http.MediaType}
         * @category 推荐接口调用者请求时ContentType
         * */
        public String consume;
        /**
         * 响应类型
         * @category 接口调用者接收返回数据的默认类型
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
        public int queue;

        /**
         * 路径
         * 与RequestMapping的机制是一样的
         * 如果用的是spring,值就和RequestMapping的值一样就行
         */
        public String value;

        /**
         * 请求方式
         */
        public RequestMethodMode methodMode;
        /**
         * 请求字段列表
         */
        public List<ApiResultField> fieldList;
    }

    public static class ApiResultField{
        /**
         * 参数名称
         * */
        public String name;

        /**
         * 字段名称
         */
        public String field;

        /**
         * 字段类型
         */
        public String fieldType;

        /**
         * 最大长度
         */
        public int maxLength;
        /**
         * 最小长度
         */
        public int minLength;
        /**
         * 备注说明
         */
        public String note;
        /**
         * 是否必填
         */
        public boolean need;

    }

    public static void main(String[] args) throws Exception {
        System.out.println(JSONArray.toJSONString(nozzle("com.tl.controller.api")));
    }
}
