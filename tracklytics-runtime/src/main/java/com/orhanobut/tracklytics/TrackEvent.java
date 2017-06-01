package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Triggers an event
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackEvent {
  String value();

  int[] filters() default {};

  String[] tags() default {};
}