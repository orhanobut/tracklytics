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

  private Tracker tracker;

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
    String eventName;
    Map<String, Object> attributes = new HashMap<>();

    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);
    eventName = trackEvent.value();

    addAttribute(method.getAnnotation(Attribute.class), attributes, result);

    addFixedAttribute(method.getAnnotation(FixedAttribute.class), attributes);
    addFixedAttributes(method.getAnnotation(FixedAttributes.class), attributes);

    Class<?> declaringClass = method.getDeclaringClass();
    addFixedAttribute(declaringClass.getAnnotation(FixedAttribute.class), attributes);
    addFixedAttributes(declaringClass.getAnnotation(FixedAttributes.class), attributes);

    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();

    generateAttributeValues(annotations, fields, attributes);

    trackEvent(eventName, attributes, superAttributes, method);

    return result;
  }

  private void addAttribute(Attribute attribute, Map<String, Object> values, Object methodResult) {
    if (attribute != null) {
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
  }

  private void addFixedAttributes(FixedAttributes fixedAttributes, Map<String, Object> values) {
    if (fixedAttributes != null) {
      FixedAttribute[] attributes = fixedAttributes.value();
      for (FixedAttribute attribute : attributes) {
        values.put(attribute.key(), attribute.value());
        if (attribute.isSuper()) {
          superAttributes.put(attribute.key(), attribute.value());
        }
      }
    }
  }

  private void addFixedAttribute(FixedAttribute attribute, Map<String, Object> values) {
    if (attribute != null) {
      values.put(attribute.key(), attribute.value());
      if (attribute.isSuper()) {
        superAttributes.put(attribute.key(), attribute.value());
      }
    }
  }

  private void generateAttributeValues(Annotation[][] keys, Object[] values, Map<String, Object> result) {
    if (keys == null || values == null) {
      return;
    }
    for (int i = 0, size = values.length; i < size; i++) {
      Object value = values[i];
      Annotation annotation = keys[i][0];
      if (annotation instanceof Attribute) {
        Attribute attribute = (Attribute) annotation;
        result.put(attribute.value(), value);
      }
      if (annotation instanceof TrackableAttribute) {
        if (value instanceof Trackable) {
          Trackable trackable = (Trackable) value;
          Map<String, String> trackableValues = trackable.getTrackableAttributes();
          if (trackableValues != null) {
            result.putAll(trackable.getTrackableAttributes());
          }
        } else {
          throw new ClassCastException("Trackable interface must be implemented for the parameter type");
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