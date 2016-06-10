package com.orhanobut.tracklytics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used along with {@link Trackable}. Trackable.getTrackableAttributes will be invoked when an event triggered
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface TrackableAttribute {
}