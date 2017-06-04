package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Prior to Java 8, the same annotation can only be used once
 * When there is a need to add multiple attributes, use this wrapper
 * <p>
 * <pre>
 *   <code>class Foo {
 *    {@literal @}FixedAttributes ({
 *       {@literal @}FixedAttribute(key="key1", value="value1"),
 *       {@literal @}FixedAttribute(key="key2", value="value2"),
 *     })
 *     public void init() {}
 *   }
 *   </code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedAttributes {
  FixedAttribute[] value();
}
