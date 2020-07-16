package org.cloud.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloud.exception.ConvertException;
import org.cloud.annotation.AliasField;
import sun.security.x509.AttributeNameEnumeration;

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
     * TODO: 优化方向
     *        1. 提供选择是否抛出异常选项
     *        2. 当setter传入参数类型与getter返回类型不一致时，用户可以提供转换方法
     */
    public static <T,S> void convert(T target, S source) throws ConvertException{
        Class<?> sourceClazz = source.getClass();
        Class<?> targetClazz = target.getClass();
        if(log.isDebugEnabled()){
            log.debug("begin to convert source["+sourceClazz+"] to target["+targetClazz+"]");
        }

        Map<String, Method> getterMethods = getPublicGetMethod(sourceClazz);
        Map<String, Method> setterMethods = getPublicSetMethod(targetClazz);
        Field[] targetFields = targetClazz.getDeclaredFields();
        for(Field field: targetFields){
            // 判断该field是否有setter方法
            String setterName = convertSetMethod(field.getName());
            if(!setterMethods.containsKey(setterName)){
                continue;
            }
            Method setterMethod = setterMethods.get(setterName);

            // 注解优先
            AliasField aliasField = field.getAnnotation(AliasField.class);
            Method getterMethod = null;
            List<String> names = new ArrayList<>();
            ConvertMethod propertyConvertMethod = null;
            if(aliasField != null){
                names.addAll(Arrays.asList(aliasField.name()));
                if(aliasField.methodName() != null && aliasField.methodName().length() > 0){
                    propertyConvertMethod = instancePropertyConvertMethod(aliasField.methodClass(), aliasField.methodName(), targetClazz, target, aliasField.parameters());
                }
            }
            names.add(field.getName());
            names = convertGetterMethod(names);
            getterMethod = findTargetMethod(names, getterMethods);

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

    private static <T> ConvertMethod instancePropertyConvertMethod(String className, String methodName, Class<?> target, T obj, Class[] params) throws ConvertException{
        /**
         * 1. className is null, get convert method from target class
         * 2. className is not null, get convert method from className's class
         * 3. methodName is not null, else return null
         */
        if(methodName == null || methodName.length() < 1){
            if(log.isDebugEnabled()){
                log.debug("method cannot be empty!");
            }
            return null;
        }

        ConvertMethod convertMethod = new ConvertMethod();
        if(className == null || className.length() < 1){
            convertMethod.clazz = target;
            try {
                convertMethod.method = convertMethod.clazz.getMethod(methodName, params);
                convertMethod.clazz = target;
                int modifiers = convertMethod.method.getModifiers();
                convertMethod.isStatic = Modifier.isStatic(modifiers);
                if(!Modifier.isStatic(modifiers)){
                    convertMethod.obj = obj;
                }
            }
            catch (NoSuchMethodException e){
                if(log.isInfoEnabled()){
                    log.info("method ["+methodName+"] not found in class ["+target.getName()+"]", e);
                }
                throw new ConvertException(e);
            }
        }
        else {
            try {
                convertMethod.clazz = Class.forName(className);
                convertMethod.method  = convertMethod.clazz.getMethod(methodName);
                int modifiers = convertMethod.method.getModifiers();
                convertMethod.isStatic = Modifier.isStatic(modifiers);
                if(!convertMethod.isStatic){
                    convertMethod.obj = convertMethod.clazz.newInstance();
                }
                return convertMethod;
            }
            catch (ClassNotFoundException e){
                if(log.isInfoEnabled()){
                    log.info("class not found for ["+className+"]", e);
                }
                throw new ConvertException(e);
            }
            catch (NoSuchMethodException e){
                if(log.isInfoEnabled()){
                    log.info("method ["+methodName+"] not found in class ["+className+"]", e);
                }
                throw new ConvertException(e);
            }
            catch (InstantiationException | IllegalAccessException e){
                if(log.isInfoEnabled()){
                    log.info("instance class ["+className+"] error", e);
                }
                throw new ConvertException(e);
            }
        }
        return convertMethod;
    }

    private static List<String> convertGetterMethod(List<String> names){
        List<String> result = new ArrayList<>();
        for(int i=0; i<names.size(); i++){
            result.add(convertGetMethod(names.get(i)));
        }
        return result;
    }

    private static List<String> convertSetterMethod(List<String> names){
        List<String> result = new ArrayList<>();
        for(int i=0; i<names.size(); i++){
            result.add(convertSetMethod(names.get(i)));
        }
        return result;
    }

    private static Method findTargetMethod(List<String> names, Map<String, Method> methods){
        for(String name: names){
            if(methods.containsKey(name)){
                return methods.get(name);
            }
        }
        return null;
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
            throw new ConvertException(e);
        }
    }

    /**
     * @param clazz
     * @return key: 方法名称，Method当前方法
     */
    private static Map<String, Method> getPublicGetMethod(Class clazz){
        return filterTargetPrefixMethod(clazz, "get");
    }

    private static Map<String, Method> getPublicSetMethod(Class clazz){
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

    private static String convertGetMethod(String name){
        return "get"+upperFirstCharacter(name);
    }

    private static String convertSetMethod(String name){
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

class ConvertMethod {
    Method method;
    Class<?> clazz;
    boolean isStatic;
    Object obj;
}
