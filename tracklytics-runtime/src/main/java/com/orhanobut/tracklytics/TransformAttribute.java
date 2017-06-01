package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Should be used with {@link TransformAttributeMap}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TransformAttribute {

  String value();

  String defaultValue() default "";

  /**
   * Keep this event in entire app scope
   */
  boolean isSuper() default false;
}
