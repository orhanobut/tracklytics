package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This doesn't trigger any tracking event.
 * Set the attribute as super attribute. Subsequent tracking events will use this value
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackSuperAttribute {
}
