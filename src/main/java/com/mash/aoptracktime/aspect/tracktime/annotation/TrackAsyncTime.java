package com.mash.aoptracktime.aspect.tracktime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackAsyncTime {
    String groupName() default "async";

    boolean ignoreOnException() default false;
}