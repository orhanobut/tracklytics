package com.orhanobut.tracklytics;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class TrackerAspect {

  private static Tracker tracker;

  private final Map<String, Object> attributes = new HashMap<>();

  private Map<String, Object> superAttributes;
  private Map<Integer, String> transformMap;

  public static void init(Tracker tracker) {
    TrackerAspect.tracker = tracker;
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackSuperAttribute * *(..))")
  public void methodAnnotatedWithSuperAttribute() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackSuperAttribute *.new(..))")
  public void constructorAnnotatedWithSuperAttribute() {
  }

  @Around("methodAnnotatedWithSuperAttribute() || constructorAnnotatedWithSuperAttribute()")
  public void weaveJoinPointSuperAttribute(ProceedingJoinPoint joinPoint) throws Throwable {
    superAttributes = tracker.superAttributes;

    // method attributes
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
//    Object methodReturn = joinPoint.proceed();

//    Attribute attribute = method.getAnnotation(Attribute.class);
    // TODO: 04/07/16 add method attributes

    // method parameters
    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();
    addSuperAttributesFromParameters(annotations, fields);
  }

  private void addSuperAttributesFromParameters(Annotation[][] keys, Object[] values) {
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
        superAttributes.put(attribute.value(), result);
      }
    }
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.RemoveSuperAttribute * *(..))")
  public void methodAnnotatedWithRemoveSuperAttribute() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.RemoveSuperAttribute *.new(..))")
  public void constructorAnnotatedWithRemoveSuperAttribute() {
  }

  @Around("methodAnnotatedWithRemoveSuperAttribute() || constructorAnnotatedWithRemoveSuperAttribute()")
  public void weaveJoinPointRemoveSuperAttribute(ProceedingJoinPoint joinPoint) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    RemoveSuperAttribute removeSuperAttribute = method.getAnnotation(RemoveSuperAttribute.class);
    tracker.removeSuperAttribute(removeSuperAttribute.value());
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
    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanosMethod = System.nanoTime();

    setup();

    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

    addClassAttributes(method, joinPoint);

    addMethodAttributes(method, result);

    addMethodParameterAttributes(method, joinPoint);

    // send the results
    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);

    pushEvent(trackEvent);

    long stopNanosTracking = System.nanoTime();
    tracker.log(startNanos, stopNanosMethod, stopNanosTracking, trackEvent, attributes, superAttributes);
    return result;
  }

  private void setup() {
    attributes.clear();
    transformMap = null;
    superAttributes = tracker.superAttributes;
  }

  private void addClassAttributes(Method method, JoinPoint joinPoint) {
    Class<?> declaringClass = method.getDeclaringClass();

    addScreenNameAttribute(declaringClass.getAnnotation(ScreenNameAttribute.class), joinPoint, attributes);

    if (method.isAnnotationPresent(TrackableAttribute.class) && Trackable.class.isAssignableFrom(declaringClass)) {
      Trackable trackable = (Trackable) joinPoint.getThis();
      if (trackable.getTrackableAttributes() != null) {
        attributes.putAll(trackable.getTrackableAttributes());
      }
    }

    while (declaringClass != null) {
      addFixedAttribute(declaringClass.getAnnotation(FixedAttribute.class));
      addFixedAttributes(declaringClass.getAnnotation(FixedAttributes.class));
      declaringClass = declaringClass.getEnclosingClass();
    }

    declaringClass = joinPoint.getThis().getClass();
    addFixedAttribute(declaringClass.getAnnotation(FixedAttribute.class));
    addFixedAttributes(declaringClass.getAnnotation(FixedAttributes.class));
  }

  private void addMethodAttributes(Method method, Object returnValue) {
    Annotation[] annotations = method.getDeclaredAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation instanceof Attribute) {
        addAttribute((Attribute) annotation, returnValue);
      }
      if (annotation instanceof FixedAttribute) {
        addFixedAttribute((FixedAttribute) annotation);
      }
      if (annotation instanceof FixedAttributes) {
        addFixedAttributes((FixedAttributes) annotation);
      }
      if (annotation instanceof TransformAttributeMap) {
        TransformAttributeMap transformAttributeMap = (TransformAttributeMap) annotation;
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
      if (annotation instanceof TransformAttribute) {
        addTransformAttribute((TransformAttribute) annotation, returnValue, transformMap);
      }
    }
  }

  private void addMethodParameterAttributes(Method method, JoinPoint joinPoint) {
    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();
    checkParameters(annotations, fields, transformMap);
  }

  private void addScreenNameAttribute(ScreenNameAttribute annotation, JoinPoint joinPoint,
                                      Map<String, Object> attributes) {
    if (annotation == null) return;
    String className = joinPoint.getThis().getClass().getSimpleName();
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

  private void addAttribute(Attribute attribute, Object methodResult) {
    if (attribute == null) return;

    Object value = null;
    if (methodResult != null) {
      value = methodResult;
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    attributes.put(attribute.value(), value);
    if (attribute.isSuper()) {
      superAttributes.put(attribute.value(), value);
    }
  }

  private void addTransformAttribute(TransformAttribute attribute, Object result, Map<Integer, String> transformMap) {
    if (attribute == null) return;

    Object value = null;
    if (result != null) {
      value = transformMap.get(result);
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    attributes.put(attribute.value(), value);
    if (attribute.isSuper()) {
      superAttributes.put(attribute.value(), value);
    }
  }

  private void addFixedAttributes(FixedAttributes fixedAttributes) {
    if (fixedAttributes == null) return;

    FixedAttribute[] attributeList = fixedAttributes.value();
    for (FixedAttribute attribute : attributeList) {
      attributes.put(attribute.key(), attribute.value());
      if (attribute.isSuper()) {
        superAttributes.put(attribute.key(), attribute.value());
      }
    }
  }

  private void addFixedAttribute(FixedAttribute attribute) {
    if (attribute == null) return;
    attributes.put(attribute.key(), attribute.value());
    if (attribute.isSuper()) {
      superAttributes.put(attribute.key(), attribute.value());
    }
  }

  private void checkParameters(Annotation[][] keys, Object[] values, Map<Integer, String> transformAttributeMap) {
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
          Map<String, Object> trackableValues = trackable.getTrackableAttributes();
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

  private void pushEvent(TrackEvent trackEvent) {
    if (tracker == null) return;
    tracker.event(trackEvent, attributes, superAttributes);
  }

}