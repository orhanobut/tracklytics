package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ScreenNameAttribute {
  String key();

  int excludeLast();

  String delimiter() default " ";
}
