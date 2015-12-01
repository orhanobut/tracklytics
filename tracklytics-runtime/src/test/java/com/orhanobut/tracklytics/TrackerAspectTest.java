package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackerType;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackerAspectTest {

  static class FooBar {

    @TrackEvent("title") void noValue() {
    }

    @TrackEvent("title") void foo(@TrackValue("key") String param) {
    }

    @TrackEvent("title") @TrackValue("key") String fooReturn() {
      return "test";
    }

    @TrackEvent("title") @TrackValue("key1") String fooReturnAndParam(@TrackValue("key2") String param) {
      return "test";
    }

    @TrackFilter(TrackerType.ADJUST)
    @TrackEvent("title") void trackEventFilter() {
    }

    @Tracklytics(TrackerAction.INIT) Tracker init() {
      return Tracker.init();
    }

    @Tracklytics(TrackerAction.START) Tracker start() {
      return Tracker.init();
    }

    @Tracklytics(TrackerAction.STOP) Tracker stop() {
      return Tracker.init();
    }
  }

  @Test public void weaveJointTracklyticsShouldInvokeInit() throws Throwable {
    TrackerAspect aspect = spy(new TrackerAspect());
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("init");

    when(methodSignature.getMethod()).thenReturn(method);

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect).init(any());
  }

  @Test public void weaveJointTracklyticsShouldInvokeStart() throws Throwable {
    TrackerAspect aspect = spy(new TrackerAspect());
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("start");

    when(methodSignature.getMethod()).thenReturn(method);

    Tracker tracker = mock(Tracker.class);
    aspect.init(tracker);

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect).start();
  }

  @Test public void weaveJointTracklyticsShouldInvokeStop() throws Throwable {
    TrackerAspect aspect = spy(new TrackerAspect());
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("stop");

    when(methodSignature.getMethod()).thenReturn(method);

    Tracker tracker = mock(Tracker.class);
    aspect.init(tracker);

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect).stop();
  }

  @Test public void weaveJointShouldInvokeTrackEvent() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    Tracker tracker = Tracker.init(trackingAdapter);

    TrackerAspect aspect = spy(new TrackerAspect());
    aspect.init(tracker);
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(joinPoint.getArgs()).thenReturn(new Object[]{"value"});

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("foo", String.class);

    when(methodSignature.getMethod()).thenReturn(method);

    aspect.weaveJoinPoint(joinPoint);

    verify(aspect).generateValues(any(Annotation[][].class), any(Object[].class), anyMap());
    verify(aspect).trackEvent(eq("title"), anyMapOf(String.class, Object.class), isNull(TrackerType[].class));
    verify(trackingAdapter).trackEvent(eq("title"), anyMapOf(String.class, Object.class));
  }

  @Test public void startShouldInvokeTrackerStart() {
    Tracker tracker = mock(Tracker.class);
    TrackerAspect trackerAspect = new TrackerAspect();
    trackerAspect.init(tracker);
    trackerAspect.start();

    verify(tracker).start();
  }

  @Test public void stopShouldInvokeTrackerStop() {
    Tracker tracker = mock(Tracker.class);
    TrackerAspect trackerAspect = new TrackerAspect();
    trackerAspect.init(tracker);
    trackerAspect.stop();

    verify(tracker).stop();
  }

  @Test public void trackEventShouldUseNoValue() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    Tracker tracker = Tracker.init(trackingAdapter);

    TrackerAspect aspect = spy(new TrackerAspect());
    aspect.init(tracker);
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("noValue");

    when(methodSignature.getMethod()).thenReturn(method);

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).hasSize(0);
  }

  @Test public void trackEventShouldUseReturnValue() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    Tracker tracker = Tracker.init(trackingAdapter);

    TrackerAspect aspect = spy(new TrackerAspect());
    aspect.init(tracker);
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("fooReturn");

    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.proceed()).thenReturn("test");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).hasSize(1);
    assertThat(argument.getValue().get("key")).isEqualTo("test");
  }

  @Test public void trackEventShouldUseReturnValueAndParameters() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    Tracker tracker = Tracker.init(trackingAdapter);

    TrackerAspect aspect = spy(new TrackerAspect());
    aspect.init(tracker);
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("fooReturnAndParam", String.class);

    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.proceed()).thenReturn("test");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).hasSize(2);
    assertThat(argument.getValue()).containsKeys("key1", "key2");
    assertThat(argument.getValue().get("key1")).isEqualTo("test");
    assertThat(argument.getValue().get("key2")).isEqualTo("param");
  }

  @Test public void testFilters() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    Tracker tracker = Tracker.init(trackingAdapter);

    TrackerAspect aspect = spy(new TrackerAspect());
    aspect.init(tracker);
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature methodSignature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("trackEventFilter");

    when(methodSignature.getMethod()).thenReturn(method);

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<TrackerType[]> argument = ArgumentCaptor.forClass(TrackerType[].class);

    verify(aspect).trackEvent(eq("title"), anyMap(), argument.capture());

    assertThat(argument.getValue()).hasSize(1);
  }

}