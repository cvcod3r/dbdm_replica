package com.dbms.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)//注解放置的目标位置即方法级别
@Retention(RetentionPolicy.RUNTIME)//注解在哪个阶段执行
@Documented
public @interface SysLogAnnotation {

    String moduleName() default ""; // 操作模块

    String childModule() default ""; // 子模块

    String operation() default "";  // 操作

    String procedureInfo() default "";  // 操作过程信息

}
