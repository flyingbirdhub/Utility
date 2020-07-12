package org.cloud.convert;

import org.cloud.ConvertException;
import org.cloud.annotation.AliasField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供类型转换方法，根据注解定义进行转换
 */
public class Convert {
    /**
     * @param target 对应属性需要有相应的set方法方能赋值成功
     * @param source 对应属性需要有相应的get方法访问获取值
     * TODO: 优化方向
     *        1. 提供选择是否抛出异常选项
     *        2. 当setter传入参数类型与getter返回类型不一致时，用户可以提供转换方法
     */
    public static <T,S> void convert(T target, S source) throws ConvertException{
        Class sourceClazz = source.getClass();
        Class targetClazz = target.getClass();
        Map<String, Method> getterMethods = getPublicGetMethod(sourceClazz);
        Map<String, Method> setterMethods = getPublicSetMethod(targetClazz);
        Field[] sourceFields = sourceClazz.getDeclaredFields();
        Field[] targetFields = targetClazz.getDeclaredFields();
        for(Field field: targetFields){
            // 判断该field是否有setter方法
            String setterName = convertSetMethod(field.getName());
            if(!setterMethods.containsKey(setterName)){
                continue;
            }
            Method setterMethod = setterMethods.get(setterMethods);

            // 注解优先
            AliasField aliasField = field.getAnnotation(AliasField.class);
            Method getterMethod = null;
            if(aliasField != null){
                String[] names = aliasField.name();
                for(String name: names){
                    String getterName = convertGetMethod(name);
                    if(getterMethods.containsKey(getterName)){
                        getterMethod = getterMethods.get(getterName);
                        break;
                    }
                }
            }

            // 没有找到注解，则用属性名称
            if(getterMethod == null){
                String getterName = convertGetMethod(field.getName());
                if(getterMethods.containsKey(getterName)){
                    getterMethod = getterMethods.get(getterName);
                }
            }

            // 注入属性值
            if(getterMethod != null){
                convertValue(target, setterMethod, source, getterMethod);
            }
        }
    }

    private static <T, S> void convertValue(T target, Method setter, S source, Method getter) throws ConvertException {
        try {
            setter.invoke(target, getter.invoke(source));
        }
        catch (Exception e){
            // 包装后重新抛出
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
