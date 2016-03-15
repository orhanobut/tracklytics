package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackerType;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TrackerAspectTest {

  static class FooBar {

    @TrackEvent("title") void noValue() {
    }

    @TrackEvent("title") void foo(@Attribute("key") String param) {
    }

    @TrackEvent("title") @Attribute("key") String fooReturn() {
      return "test";
    }

    @TrackEvent("title") @Attribute("key1") String fooReturnAndParam(@Attribute("key2") String param) {
      return "test";
    }

    @TrackFilter(TrackerType.MIXPANEL)
    @TrackEvent("title") void trackEventFilter() {
    }

    @TrackEvent("title")
    @Attribute(value = "key1", defaultResult = "defaultResult") void trackDefaultValue() {
    }

    @TrackEvent("title")
    @Attribute(value = "key1", defaultResult = "defaultResult") String trackDefaultValueWithReturn() {
      return "returnValue";
    }

    @Track(eventName = "Event", attributeKey = "key", attributeValue = "value") void trackWithTrack() {
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

  Tracker tracker;
  FooBar fooBar;

  @Spy TrackerAspect aspect;
  @Mock ProceedingJoinPoint joinPoint;
  @Mock MethodSignature methodSignature;
  @Mock TrackingAdapter trackingAdapter;
  @Captor ArgumentCaptor<Map<String, Object>> valueMapCaptor;

  @Before public void setup() throws Exception {
    initMocks(this);

    fooBar = new FooBar();

    when(joinPoint.getSignature()).thenReturn(methodSignature);

    tracker = spy(Tracker.init(trackingAdapter));
    aspect.init(tracker);
  }

  @Test public void invokeInit() throws Throwable {
    initMethod("init");

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect, times(2)).init(any());
  }

  @Test public void invokeStart() throws Throwable {
    initMethod("start");

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect).start();
  }

  @Test public void invokeStop() throws Throwable {
    initMethod("stop");

    aspect.weaveJointTracklytics(joinPoint);

    verify(aspect).stop();
  }

  @Test public void invokeTrackEvent() throws Throwable {
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(joinPoint.getArgs()).thenReturn(new Object[]{"value"});

    FooBar fooBar = new FooBar();
    Method method = fooBar.getClass().getDeclaredMethod("foo", String.class);

    when(methodSignature.getMethod()).thenReturn(method);

    aspect.weaveJoinPoint(joinPoint);

    verify(aspect).generateFieldValues(any(Annotation[][].class), any(Object[].class), anyMap());
    verify(aspect).trackEvent(eq("title"), anyMapOf(String.class, Object.class), isNull(TrackerType[].class));
    verify(trackingAdapter).trackEvent(eq("title"), anyMapOf(String.class, Object.class));
  }

  @Test public void invokeTrackerStart() {
    aspect.start();

    verify(tracker).start();
  }

  @Test public void invokeTrackerStop() {
    aspect.stop();

    verify(tracker).stop();
  }

  @Test public void trackEventShouldUseNoValue() throws Throwable {
    initMethod("noValue");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).isEmpty();
  }

  @Test public void trackEventShouldUseReturnValue() throws Throwable {
    initMethod("fooReturn");
    when(joinPoint.proceed()).thenReturn("test");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).containsEntry("key", "test");
  }

  @Test public void trackEventShouldUseReturnValueAndParameters() throws Throwable {
    initMethod("fooReturnAndParam", String.class);
    when(joinPoint.proceed()).thenReturn("test");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(aspect).trackEvent(eq("title"), argument.capture(), isNull(TrackerType[].class));

    assertThat(argument.getValue()).containsOnlyKeys("key1", "key2");
    assertThat(argument.getValue().get("key1")).isEqualTo("test");
    assertThat(argument.getValue().get("key2")).isEqualTo("param");
  }

  @Test public void useDefaultValueOnTrackValueWhenItIsSet() throws Throwable {
    initMethod("trackDefaultValue");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<TrackerType[]> argument = ArgumentCaptor.forClass(TrackerType[].class);
    verify(aspect).trackEvent(eq("title"), valueMapCaptor.capture(), argument.capture());

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultResult");
  }

  @Test public void useDefaultValueOnTrackValueWhenThereIsReturnValueAndItIsSet() throws Throwable {
    initMethod("trackDefaultValueWithReturn");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<TrackerType[]> argument = ArgumentCaptor.forClass(TrackerType[].class);
    verify(aspect).trackEvent(eq("title"), valueMapCaptor.capture(), argument.capture());

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultResult");
  }

  @Test public void testFilters() throws Throwable {
    initMethod("trackEventFilter");

    aspect.weaveJoinPoint(joinPoint);

    ArgumentCaptor<TrackerType[]> argument = ArgumentCaptor.forClass(TrackerType[].class);

    verify(aspect).trackEvent(eq("title"), anyMap(), argument.capture());

    assertThat(argument.getValue()).hasSize(1);
  }

  @Test public void testTrack() throws Throwable {
    initMethod("trackWithTrack");

    aspect.weaveJoinPoint(joinPoint);

    verify(aspect).trackEvent(eq("Event"), valueMapCaptor.capture(), any(TrackerType[].class));
    assertThat(valueMapCaptor.getValue()).containsEntry("key", "value");
  }

  void initMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
    Method method = fooBar.getClass().getDeclaredMethod(name, parameterTypes);
    when(methodSignature.getMethod()).thenReturn(method);
  }

}