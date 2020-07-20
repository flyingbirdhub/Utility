package org.cloud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AliasField {
    /**
     * 可供拷贝的属性的名称，以注解中注册的名称优先，寻找名称对应的getter函数
     * 第一个找到的getter函数即为源
     */
    String[] name() default {};

    /**
     * 将源值转换为目标值的方法所在的类，默认为目标对象
     */
    Class methodClass() default Class.class;

    /**
     * 将源值转换为目标值的方法名称
     */
    String methodName() default "";

    /**
     * 将源值转换为目标值的方法需要的参数
     */
    Class[] parameters() default {};
}
