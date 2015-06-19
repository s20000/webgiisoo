package com.giisoo.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBMapping {

	String db() default X.EMPTY;

	String table() default X.EMPTY;

	String collection() default X.EMPTY;

}
