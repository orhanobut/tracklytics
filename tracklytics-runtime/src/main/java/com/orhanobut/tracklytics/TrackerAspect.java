package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackerType;

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

  @Pointcut("execution(@com.orhanobut.tracklytics.Track * *(..))")
  public void methodAnnotatedWithTrack() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.Track *.new(..))")
  public void constructorAnnotatedTrack() {
  }

  @Around("methodAnnotatedWithTrack() || constructorAnnotatedTrack()")
  public Object weaveJoinPointTrack(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Object result = joinPoint.proceed();

    Method method = methodSignature.getMethod();
    String eventName;
    Map<String, Object> values = new HashMap<>();

    Track track = method.getAnnotation(Track.class);
    eventName = track.eventName();
    values.put(track.attributeKey(), track.attributeValue());

    trackEvent(eventName, values, method);

    return result;
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
    Map<String, Object> values = new HashMap<>();

    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);
    eventName = trackEvent.value();

    Attribute methodAttribute = method.getAnnotation(Attribute.class);
    if (methodAttribute != null) {
      String defaultValue = methodAttribute.defaultResult();
      if (defaultValue != null && defaultValue.trim().length() != 0) {
        values.put(methodAttribute.value(), methodAttribute.defaultResult());
      } else {
        values.put(methodAttribute.value(), result);
      }
    }

    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();
    generateFieldValues(annotations, fields, values);

    trackEvent(eventName, values, method);

    return result;
  }

  void generateFieldValues(Annotation[][] keys, Object[] values, Map<String, Object> result) {
    if (keys == null || values == null) {
      return;
    }
    for (int i = 0, size = values.length; i < size; i++) {
      Attribute attribute = (Attribute) keys[i][0];
      if (attribute == null) {
        continue;
      }
      Object value = values[i];
      result.put(attribute.value(), value);
    }
  }

  void trackEvent(String title, Map<String, Object> values, Method method) {
    if (tracker == null) {
      return;
    }
    TrackFilter trackFilter = method.getAnnotation(TrackFilter.class);
    TrackerType[] filters = null;
    if (trackFilter != null) {
      filters = trackFilter.value();
    }

    Set<Integer> filter = Collections.emptySet();
    if (filters != null) {
      filter = new HashSet<>(filters.length);
      for (TrackerType tracker : filters) {
        filter.add(tracker.getValue());
      }
    }
    tracker.event(title, values, filter);
  }

}