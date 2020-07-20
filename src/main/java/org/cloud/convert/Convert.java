package org.cloud.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloud.exception.ConvertException;
import org.cloud.annotation.AliasField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 提供类型转换方法，根据注解定义进行转换
 */
public class Convert {
    private final static Log log = LogFactory.getLog(Convert.class);

    /**
     * @param target 对应属性需要有相应的set方法方能赋值成功
     * @param source 对应属性需要有相应的get方法访问获取值
     */
    public static <T,S> void convert(T target, S source) throws ConvertException{
        Class sourceClazz = source.getClass();
        Class targetClazz = target.getClass();
        if(log.isDebugEnabled()){
            log.debug("begin to convert source["+sourceClazz+"] to target["+targetClazz+"]");
        }

        Map<String, Method> getterMethods = ConvertUtil.getPublicGetMethod(sourceClazz);
        Map<String, Method> setterMethods = ConvertUtil.getPublicSetMethod(targetClazz);
        Field[] targetFields = targetClazz.getDeclaredFields();
        for(Field field: targetFields){
            // 判断该field是否有setter方法
            String setterName = ConvertUtil.convertSetMethod(field.getName());
            if(!setterMethods.containsKey(setterName)){
                continue;
            }
            Method setterMethod = setterMethods.get(setterName);

            // 注解优先
            AliasField aliasField = field.getAnnotation(AliasField.class);
            Method getterMethod;
            List<String> names = new ArrayList<>();
            ConvertMethod propertyConvertMethod = null;
            if(aliasField != null){
                names.addAll(Arrays.asList(aliasField.name()));
                if(aliasField.methodName() != null && aliasField.methodName().length() > 0){
                    propertyConvertMethod = instancePropertyConvertMethod(aliasField.methodClass(), aliasField.methodName(), target, aliasField.parameters());
                }
            }
            names.add(field.getName());
            names = ConvertUtil.convertGetterMethod(names);
            getterMethod = ConvertUtil.findTargetMethod(names, getterMethods);

            // 注入属性值
            if(getterMethod != null){
                // 转换函数
                if(log.isDebugEnabled()){
                    log.debug("convert property ["+field.getName()+"]");
                }
                convertValue(target, setterMethod, source, getterMethod, propertyConvertMethod);
            }
        }
    }

    private static <T> ConvertMethod instancePropertyConvertMethod(Class convertClazz, String convertMethodName, T obj, Class[] params) throws ConvertException{
        /**
         * 1. className is null, get convert method from target class
         * 2. className is not null, get convert method from className's class
         * 3. methodName is not null, else return null
         */
        if(convertMethodName == null || convertMethodName.length() < 1){
            if(log.isDebugEnabled()){
                log.debug("method cannot be empty!");
            }
            return null;
        }

        Class targetClazz = convertClazz;
        boolean isConvertClazz = true;
        if(convertClazz==null || convertClazz==Class.class){
            isConvertClazz = false;
            targetClazz = obj.getClass();
        }

        ConvertMethod convertMethod = new ConvertMethod();
        try {
            convertMethod.method = targetClazz.getMethod(convertMethodName, params);
            int modifiers = convertMethod.method.getModifiers();
            if(!Modifier.isStatic(modifiers)){
                if(!isConvertClazz){
                    convertMethod.obj = obj;
                }
                else {
                    convertMethod.obj = targetClazz.newInstance();
                }
            }
        }
        catch (NoSuchMethodException e){
            if(log.isInfoEnabled()){
                log.info("method ["+convertMethodName+"] not found in class ["+targetClazz.getName()+"]", e);
            }
            throw new ConvertException(e);
        }
        catch (InstantiationException | IllegalAccessException e){
            if(log.isInfoEnabled()){
                log.info("instance class ["+convertClazz.getName()+"] error", e);
            }
            throw new ConvertException(e);
        }
        return convertMethod;
    }

    private static <T, S> void convertValue(T target, Method setter, S source, Method getter, ConvertMethod convertMethod) throws ConvertException {
        try {
            Object param = getter.invoke(source);
            if(convertMethod != null){
                param = convertMethod.method.invoke(convertMethod.obj, param);
            }
            setter.invoke(target, param);
        }
        catch (Exception e){
            if(log.isInfoEnabled()){
                log.info("convert property failed, setter["+setter.getName()+"], getter["+getter.getName()+"]");
            }
            throw new ConvertException(e);
        }
    }
}

class ConvertMethod {
    // target method
    Method method;
    // target obj invoked by method
    Object obj;
}

class ConvertUtil {
    public static List<String> convertGetterMethod(List<String> names){
        List<String> result = new ArrayList<>();
        for(int i=0; i<names.size(); i++){
            result.add(ConvertUtil.convertGetMethod(names.get(i)));
        }
        return result;
    }

    public static Method findTargetMethod(List<String> names, Map<String, Method> methods){
        for(String name: names){
            if(methods.containsKey(name)){
                return methods.get(name);
            }
        }
        return null;
    }

    public static Map<String, Method> getPublicGetMethod(Class clazz){
        return filterTargetPrefixMethod(clazz, "get");
    }

    public static Map<String, Method> getPublicSetMethod(Class clazz){
        return filterTargetPrefixMethod(clazz, "set");
    }

    private static Map<String, Method> filterTargetPrefixMethod(Class clazz, String prefix){
        assert prefix != null && prefix.length()>0;

        List<Method> methods = getDeclaredPublicMethod(clazz);
        Map<String, Method> getMethods = new HashMap<String, Method>();
        for(Method method: methods){
            if(method.getName().startsWith(prefix)){
                getMethods.put(method.getName(), method);
            }
        }
        return getMethods;
    }

    private static List<Method> getDeclaredPublicMethod(Class clazz){
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> methods = new ArrayList<Method>();
        for(Method method: declaredMethods){
            if(Modifier.isPublic(method.getModifiers())){
                methods.add(method);
            }
        }
        return methods;
    }

    public static String convertGetMethod(String name){
        return "get"+upperFirstCharacter(name);
    }

    public static String convertSetMethod(String name){
        return "set"+upperFirstCharacter(name);
    }

    private static String upperFirstCharacter(String name){
        assert name != null && name.length()>0;
        if(name.length() < 2){
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase()+name.substring(1);
    }
}
