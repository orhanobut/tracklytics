package com.orhanobut.tracklytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides attributes (metadata) for the corresponding event.
 * <p>
 * {@link Attribute} can be used for the following use cases:
 * <ul>
 *   <li>On method signature:</li>
 *   When the method returns a value and you want to be able to use this value for attribute,
 *   use it on the method signature. For the following case, returned value ("value") will be
 *   used for the attribute ("key")
 *   <pre>
 *     <code>class Foo{
 *      {@literal @}TrackEvent("event_name")
 *      {@literal @}Attribute(key="key")
 *       public String foo(){
 *         return "value"
 *       }
 *     }
 *     </code>
 *   </pre>
 *   <li>On method parameter:</li>
 *   When you want to use any method parameter as attribute, annotate them with Attribute
 *   For the following example, "name" value will be used for attribute value
 *   <pre>
 *     <code>class Foo{
 *      {@literal @}TrackEvent("event_name")
 *       public void foo({@literal @}Attribute(key="key") String name){
 *       }
 *     }
 *     </code>
 *   </pre>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
  String value();

  String defaultValue() default "";

  /**
   * Sets the attribute as super attribute
   */
  boolean isSuper() default false;
}
