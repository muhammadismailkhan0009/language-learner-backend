package com.myriadcode.languagelearner.code_generation_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateOperation {

    String goal();

    Class<?>[] deps();
}
