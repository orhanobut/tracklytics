package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Removes any existing super attribute.
 * Subsequent tracking events will not use this super attribute any longer
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoveSuperAttribute {

  String value();
}
