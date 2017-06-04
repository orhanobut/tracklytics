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
@SuppressWarnings("WeakerAccess")
public class TracklyticsAspect {

  private static AspectListener aspectListener;

  static void subscribe(AspectListener listener) {
    TracklyticsAspect.aspectListener = listener;
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackSuperAttribute * *(..))")
  public void methodAnnotatedWithSuperAttribute() {
    // No implementation is needed
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackSuperAttribute *.new(..))")
  public void constructorAnnotatedWithSuperAttribute() {
    // No implementation is needed
  }

  @Around("methodAnnotatedWithSuperAttribute() || constructorAnnotatedWithSuperAttribute()")
  public void weaveJoinPointSuperAttribute(ProceedingJoinPoint joinPoint) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

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
        addSuperAttribute(attribute.value(), result);
      }
    }
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.RemoveSuperAttribute * *(..))")
  public void methodAnnotatedWithRemoveSuperAttribute() {
    // No implementation is needed
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.RemoveSuperAttribute *.new(..))")
  public void constructorAnnotatedWithRemoveSuperAttribute() {
    // No implementation is needed
  }

  @Around("methodAnnotatedWithRemoveSuperAttribute() || constructorAnnotatedWithRemoveSuperAttribute()")
  public void weaveJoinPointRemoveSuperAttribute(ProceedingJoinPoint joinPoint) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    RemoveSuperAttribute removeSuperAttribute = method.getAnnotation(RemoveSuperAttribute.class);
    removeSuperAttribute(removeSuperAttribute.value());
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent * *(..))")
  public void methodAnnotatedWithTrackEvent() {
    // No implementation is needed
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent *.new(..))")
  public void constructorAnnotatedTrackEvent() {
    // No implementation is needed
  }

  @Around("methodAnnotatedWithTrackEvent() || constructorAnnotatedTrackEvent()")
  public Object weaveJoinPointTrackEvent(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();

    // Local attributes
    final Map<String, Object> attributes = new HashMap<>();

    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

    addClassAttributes(method, joinPoint, attributes);

    // TODO: This creation is most of the time redundant. Find a way to avoid redundant map creation
    Map<Integer, String> transformMap = new HashMap<>();
    addMethodAttributes(method, result, attributes, transformMap);
    addMethodParameterAttributes(method, joinPoint, attributes, transformMap);

    // send the results
    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);

    pushEvent(trackEvent, attributes);
    return result;
  }

  private void addClassAttributes(Method method, JoinPoint joinPoint, Map<String, Object> attributes) {
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
  }

  private void addMethodAttributes(Method method, Object returnValue, Map<String, Object> attributes,
                                   Map<Integer, String> transformMap) {
    Annotation[] annotations = method.getDeclaredAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation instanceof Attribute) {
        addAttribute((Attribute) annotation, returnValue, attributes);
      }
      if (annotation instanceof FixedAttribute) {
        addFixedAttribute((FixedAttribute) annotation, attributes);
      }
      if (annotation instanceof FixedAttributes) {
        addFixedAttributes((FixedAttributes) annotation, attributes);
      }
      if (annotation instanceof TransformAttributeMap) {
        TransformAttributeMap transformAttributeMap = (TransformAttributeMap) annotation;
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
        addTransformAttribute((TransformAttribute) annotation, returnValue, transformMap, attributes);
      }
    }
  }

  private void addMethodParameterAttributes(Method method, JoinPoint joinPoint, Map<String, Object> attributes,
                                            Map<Integer, String> transformMap) {
    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();
    checkParameters(annotations, fields, transformMap, attributes);
  }

  private void addAttribute(Attribute attribute, Object methodResult, Map<String, Object> attributes) {
    if (attribute == null) return;

    Object value = null;
    if (methodResult != null) {
      value = methodResult;
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    attributes.put(attribute.value(), value);
    if (attribute.isSuper()) {
      addSuperAttribute(attribute.value(), value);
    }
  }

  private void addTransformAttribute(TransformAttribute attribute, Object result, Map<Integer, String> transformMap,
                                     Map<String, Object> attributes) {
    if (attribute == null) return;

    Object value = null;
    if (result != null) {
      value = transformMap.get(result);
    } else if (attribute.defaultValue().length() != 0) {
      value = attribute.defaultValue();
    }
    attributes.put(attribute.value(), value);
    if (attribute.isSuper()) {
      addSuperAttribute(attribute.value(), value);
    }
  }

  private void addFixedAttributes(FixedAttributes fixedAttributes, Map<String, Object> attributes) {
    if (fixedAttributes == null) return;

    FixedAttribute[] attributeList = fixedAttributes.value();
    for (FixedAttribute attribute : attributeList) {
      attributes.put(attribute.key(), attribute.value());
      if (attribute.isSuper()) {
        addSuperAttribute(attribute.key(), attribute.value());
      }
    }
  }

  private void addFixedAttribute(FixedAttribute attribute, Map<String, Object> attributes) {
    if (attribute == null) return;
    attributes.put(attribute.key(), attribute.value());
    if (attribute.isSuper()) {
      addSuperAttribute(attribute.key(), attribute.value());
    }
  }

  private void checkParameters(Annotation[][] keys, Object[] values, Map<Integer, String> transformAttributeMap,
                               Map<String, Object> attributes) {
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
          addSuperAttribute(attribute.value(), result);
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
          addSuperAttribute(transformAttribute.value(), result);
        }
      }
    }
  }

  private void pushEvent(TrackEvent trackEvent, Map<String, Object> attributes) {
    if (aspectListener == null) return;

    aspectListener.onAspectEventTriggered(trackEvent, attributes);
  }

  private void addSuperAttribute(String key, Object value) {
    if (aspectListener == null) return;

    aspectListener.onAspectSuperAttributeAdded(key, value);
  }

  private void removeSuperAttribute(String key) {
    if (aspectListener == null) return;

    aspectListener.onAspectSuperAttributeRemoved(key);
  }

}