package com.myriadcode.languagelearner.code_generation_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateMethod {

    String goal();

    Class<?>[] deps();

    String[] invariants() default {};

    String generate() default "code"; // "code" | "tests-first"
}
