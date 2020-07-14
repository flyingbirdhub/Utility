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
    String methodClass() default "";

    /**
     * 定义转换类中的具体方法
     * TODO：
     * 1. methodClass为空，默认从函数本身进行方法调用
     * 2. methodClass不为空，则从指定的class方法进行调用
     */
    String methodName() default "";
}
