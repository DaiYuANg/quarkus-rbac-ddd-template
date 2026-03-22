package com.github.DaiYuANg.export.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {
    String header();
    int order() default 0;
}
