package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Maps the actual given attribute value to a custom one.
 * Should be use with {@link TransformAttribute}
 * For example:
 * <p>
 * Your attribute value might be position index but you want to track an actual value
 * by using transformAttributeMap, tracker will use the defined custom value
 * </p>
 * <pre>
 * <code>
 * class Foo {
 *     {@literal @}TrackEvent("event")
 *     {@literal @}TransformAttributeMap(
 *        keys = {0, 1},
 *        values = {"value0", "value1"}
 *      )
 *      public void foo(@TransformAttribute("key") int position) {
 *      }
 * }
 * </code></pre>
 * <p>
 * Tracklytics will use value0 as attribute value when position is 0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TransformAttributeMap {
  int[] keys();

  String[] values();
}
