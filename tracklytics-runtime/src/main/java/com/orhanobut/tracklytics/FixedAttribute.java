package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Assign a fixed/static value for the given attribute
 * This can be used in the situations that the actual value is not dynamic.
 * For example: A good case would be screen name or button name
 * <p>
 * <pre>
 *   <code>{@literal @}FixedAttribute(key="scree_name", value="Login")
 *    class Foo {
 *    }
 *   </code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedAttribute {
  String key();

  String value();

  /**
   * Keep this event in entire app scope
   */
  boolean isSuper() default false;
}
