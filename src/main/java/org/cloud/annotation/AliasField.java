package org.cloud.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AliasField {
    /**
     * 定义支持的别名列表，默认为空
     */
    String[] name() default {};

    /**
     * 定义getter的返回到setter的转换类路径
     */
    Class methodClass() default Class.class;

    /**
     * 定义转换类中的具体方法
     */
    String methodName() default "";

    /**
     * 方法的参数
     */
    Class[] parameters() default {};
}
