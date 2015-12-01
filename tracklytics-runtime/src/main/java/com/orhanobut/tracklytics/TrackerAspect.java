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

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent * *(..))")
  public void methodAnnotatedWithTrackEvent() {
  }

  @Pointcut("execution(@com.orhanobut.tracklytics.TrackEvent *.new(..))")
  public void constructorAnnotatedTrackEvent() {
  }

  @Around("methodAnnotatedWithTrackEvent() || constructorAnnotatedTrackEvent()")
  public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Object result = joinPoint.proceed();

    Method method = methodSignature.getMethod();
    TrackEvent trackEvent = method.getAnnotation(TrackEvent.class);
    String title = trackEvent.value();

    Map<String, Object> values = new HashMap<>();

    TrackValue returnField = method.getAnnotation(TrackValue.class);
    if (returnField != null) {
      values.put(returnField.value(), result);
    }

    Object[] fields = joinPoint.getArgs();
    Annotation[][] annotations = method.getParameterAnnotations();
    generateValues(annotations, fields, values);

    TrackFilter trackFilter = method.getAnnotation(TrackFilter.class);
    TrackerType[] filter = null;
    if (trackFilter != null) {
      filter = trackFilter.value();
    }

    trackEvent(title, values, filter);

    return result;
  }

  void generateValues(Annotation[][] keys, Object[] values, Map<String, Object> result) {
    if (keys == null || values == null) {
      return;
    }
    for (int i = 0, size = values.length; i < size; i++) {
      TrackValue trackValue = (TrackValue) keys[i][0];
      if (trackValue == null) {
        continue;
      }
      Object value = values[i];
      result.put(trackValue.value(), value);
    }
  }

  void trackEvent(String title, Map<String, Object> values, TrackerType[] trackers) {
    if (tracker == null) {
      return;
    }
    Set<Integer> filter = Collections.emptySet();
    if (trackers != null) {
      filter = new HashSet<>(trackers.length);
      for (TrackerType tracker : trackers) {
        filter.add(tracker.getValue());
      }
    }
    tracker.event(title, values, filter);
  }

}