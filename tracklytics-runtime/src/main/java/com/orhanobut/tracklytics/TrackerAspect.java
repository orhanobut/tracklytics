package com.orhanobut.tracklytics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Aspect
public class TrackerAspect {

  private final Map<String, Object> superAttributes = new HashMap<>();

  private static Tracker tracker;

  /**
   * Init without aspect
   */
  public static void init(Tracker tracker) {
    TrackerAspect.tracker = tracker;
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.Tracklytics * *(..))")
  public void methodAnnotatedWithTracklytics() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.Tracklytics *.new(..))")
  public void constructorAnnotatedTracklytics() {
  }

  @Around("methodAnnotatedWithTracklytics() || constructorAnnotatedTracklytics()")
  public Object weaveJointTracklytics(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Object object = joinPoint.proceed();

    Method method = methodSignature.getMethod();
    Tracklytics tracklytics = method.getAnnotation(Tracklytics.class);
    TrackerAction trackerAction = tracklytics.value();
    switch (trackerAction) {
      case INIT:
        init(object);
        break;
      case START:
        start();
        break;
      case STOP:
        stop();
        break;
      default:
        throw new Exception("This should not happen");
    }

    return tracker;
  }

  void init(Object result) {
    tracker = (Tracker) result;
  }

  void start() {
    tracker.start();
  }

  void stop() {
    tracker.stop();
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent * *(..))")
  public void methodAnnotatedWithTrackEvent() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent *.new(..))")
  public void constructorAnnotatedTrackEvent() {
  }

  @Around("methodAnnotatedWithTrackEvent() || constructorAnnotatedTrackEvent()")
  public Object weaveJoinPointTrackEvent(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Object result = joinPoint.proceed();

    Method method = methodSignature.getMethod();

    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);
    String eventName = trackEvent.value();

    Map<String, Object> attributes = new HashMap<>();

    ScreenNameAttribute screenNameAttribute = method.getDeclaringClass().getAnnotation(ScreenNameAttribute.class);
    addScreenNameAttribute(screenNameAttribute, joinPoint.getThis().getClass().getSimpleName(), attributes);

    addAttribute(method.getAnnotation(Attribute.class), attributes, result);

    Class<?> declaringClass = method.getDeclaringClass();
    if (method.isAnnotationPresent(TrackableAttribute.class) && Trackable.class.isAssignableFrom(declaringClass)) {
      Trackable trackable = (Trackable) joinPoint.getThis();
      if (trackable.getTrackableAttributes() != null) {
        attributes.putAll(trackable.getTrackableAttributes());
      }
    }

    while (declaringClass != null) {
      addFixedAttribute(declaringClass.getAnnotation(FixedAttribute.class), attributes);
      addFixedAttributes(declaringClass.getAnnotation(FixedAttributes.class), attributes);
      declaringClass = declaringClass.getEnclosingClass();
    }

    declaringClass = joinPoint.getThis().getClass();
    addFixedAttribute(declaringClass.getAnnotation(FixedAttribute.class), attributes);
    addFixedAttributes(declaringClass.getAnnotation(FixedAttributes.class), attributes);

    addFixedAttribute(method.getAnnotation(FixedAttribute.class), attributes);
    addFixedAttributes(method.getAnnotation(FixedAttributes.class), attributes);

    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();

    Map<Integer, String> transformMap = null;
    TransformAttributeMap transformAttributeMap = method.getAnnotation(TransformAttributeMap.class);
    if (transformAttributeMap != null) {
      transformMap = new HashMap<>();
      int[] keys = transformAttributeMap.keys();
      String[] values = transformAttributeMap.values();
      if (keys.length != values.length) {
        throw new IllegalStateException("TransformAttributeMap keys and values must have same length");
      }
      for (int i = 0; i < keys.length; i++) {
        transformMap.put(keys[i], values[i]);
      }
    }
    addTransformAttribute(method.getAnnotation(TransformAttribute.class), attributes, result, transformMap);

    generateAttributeValues(annotations, fields, attributes, transformMap);

    trackEvent(eventName, attributes, superAttributes, method);

    return result;
  }

  private void addScreenNameAttribute(ScreenNameAttribute annotation, String className,
                                      Map<String, Object> attributes) {
    if (annotation == null) return;

    String[] words = className.split("(?=\\p{Upper})");
    int excludeLast = annotation.excludeLast();
    StringBuilder builder = new StringBuilder();
    for (int i = 0, size = words.length - excludeLast; i < size; i++) {
      builder.append(words[i]);
      if (i < size - 1) {
        builder.append(annotation.delimiter());
      }
    }
    attributes.put(annotation.key(), builder.toString());
  }

  private void addAttribute(Attribute attribute, Map<String, Object> values, Object methodResult) {
    if (attribute == null) return;

    Object value = null;
    if (methodResult != null) {
      value = methodResult;
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    values.put(attribute.value(), value);
    if (attribute.isSuper()) {
      superAttributes.put(attribute.value(), value);
    }
  }

  private void addTransformAttribute(TransformAttribute attribute, Map<String, Object> values, Object methodResult,
                                     Map<Integer, String> transformMap) {
    if (attribute == null) return;

    Object value = null;
    if (methodResult != null) {
      value = transformMap.get(methodResult);
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    values.put(attribute.value(), value);
    if (attribute.isSuper()) {
      superAttributes.put(attribute.value(), value);
    }
  }

  private void addFixedAttributes(FixedAttributes fixedAttributes, Map<String, Object> values) {
    if (fixedAttributes == null) return;

    FixedAttribute[] attributes = fixedAttributes.value();
    for (FixedAttribute attribute : attributes) {
      values.put(attribute.key(), attribute.value());
      if (attribute.isSuper()) {
        superAttributes.put(attribute.key(), attribute.value());
      }
    }
  }

  private void addFixedAttribute(FixedAttribute attribute, Map<String, Object> values) {
    if (attribute == null) return;
    values.put(attribute.key(), attribute.value());
    if (attribute.isSuper()) {
      superAttributes.put(attribute.key(), attribute.value());
    }
  }

  private void generateAttributeValues(Annotation[][] keys, Object[] values, Map<String, Object> attributes,
                                       Map<Integer, String> transformAttributeMap) {
    if (keys == null || values == null) {
      return;
    }
    for (int i = 0, size = keys.length; i < size; i++) {
      if (keys[i].length == 0) {
        continue;
      }
      Object value = values[i];
      Annotation annotation = keys[i][0];
      if (annotation instanceof Attribute) {
        Attribute attribute = (Attribute) annotation;
        Object result = null;
        if (value != null) {
          result = value;
        } else if (attribute.defaultValue().length() != 0) {
          result = attribute.defaultValue();
        }
        attributes.put(attribute.value(), result);
        if (attribute.isSuper()) {
          superAttributes.put(attribute.value(), result);
        }
      }
      if (annotation instanceof TrackableAttribute) {
        if (value instanceof Trackable) {
          Trackable trackable = (Trackable) value;
          Map<String, String> trackableValues = trackable.getTrackableAttributes();
          if (trackableValues != null) {
            attributes.putAll(trackable.getTrackableAttributes());
          }
        } else {
          throw new ClassCastException("Trackable interface must be implemented for the parameter type");
        }
      }
      if (annotation instanceof TransformAttribute) {
        if (transformAttributeMap == null) {
          throw new IllegalStateException("Method must have TransformAttributeMap when TransformAttribute is used");
        }
        TransformAttribute transformAttribute = (TransformAttribute) annotation;
        Object result = null;
        if (value != null) {
          result = transformAttributeMap.get(value);
        } else if (transformAttribute.defaultValue().length() != 0) {
          result = transformAttribute.defaultValue();
        }

        attributes.put(transformAttribute.value(), result);
        if (transformAttribute.isSuper()) {
          superAttributes.put(transformAttribute.value(), result);
        }
      }
    }
  }

  private void trackEvent(String title, Map<String, Object> attributes, Map<String, Object> superAttributes,
                          Method method) {
    if (tracker == null) {
      return;
    }
    TrackFilter trackFilter = method.getAnnotation(TrackFilter.class);
    int[] filters = null;
    if (trackFilter != null) {
      filters = trackFilter.value();
    }

    Set<Integer> filter = Collections.emptySet();
    if (filters != null) {
      filter = new HashSet<>(filters.length);
      for (int tracker : filters) {
        filter.add(tracker);
      }
    }
    tracker.event(title, attributes, superAttributes, filter);
  }

}