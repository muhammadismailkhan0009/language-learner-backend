package com.myriadcode.languagelearner.code_generation_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateClass {

    String name();

    String goal();

    String kind() default "class"; // "class" | "interface"

    Class<?>[] deps() default {};

    Class<?>[] templates() default {};
}
