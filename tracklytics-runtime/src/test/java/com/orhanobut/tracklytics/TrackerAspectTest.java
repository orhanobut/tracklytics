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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TrackerAspectTest {

  Tracker tracker;

  TrackerAspect aspect;
  @Mock ProceedingJoinPoint joinPoint;
  @Mock MethodSignature methodSignature;
  @Mock TrackingAdapter trackingAdapter;
  @Captor ArgumentCaptor<Map<String, Object>> valueMapCaptor;

  @Before public void setup() throws Exception {
    initMocks(this);

    aspect = new TrackerAspect();

    when(joinPoint.getSignature()).thenReturn(methodSignature);

    tracker = spy(Tracker.init(trackingAdapter));
    aspect.init(tracker);
  }

  @Test public void testInit() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.INIT) Tracker init() {
        return Tracker.init();
      }
    }
    initMethod(Foo.class, "init");

    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);

    assertThat(tracker).isEqualTo(tracker);
  }

  @Test public void testStart() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.START) Tracker start() {
        return Tracker.init();
      }
    }
    initMethod(Foo.class, "start");

    aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).start();
  }

  @Test public void testStop() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.STOP) Tracker stop() {
        return Tracker.init();
      }
    }
    initMethod(Foo.class, "stop");

    aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).stop();
  }

  @Test public void trackEventShouldUseNoValue() throws Throwable {
    class Foo {
      @TrackEvent("title") void noValue() {
      }
    }
    initMethod(Foo.class, "noValue");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.<Integer>emptySet()));

    assertThat(argument.getValue()).isEmpty();
  }

  @Test public void trackEventShouldUseReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key") String fooReturn() {
        return "test";
      }
    }

    initMethod(Foo.class, "fooReturn");
    when(joinPoint.proceed()).thenReturn("test");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> values = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), values.capture(), eq(Collections.<Integer>emptySet()));

    assertThat(values.getValue()).containsEntry("key", "test");
  }

  @Test public void trackEventShouldUseReturnValueAndParameters() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key1") String fooReturnAndParam(@Attribute("key2") String param) {
        return "test";
      }
    }
    initMethod(Foo.class, "fooReturnAndParam", String.class);
    when(joinPoint.proceed()).thenReturn("test");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.<Integer>emptySet()));

    assertThat(argument.getValue()).containsOnlyKeys("key1", "key2");
    assertThat(argument.getValue().get("key1")).isEqualTo("test");
    assertThat(argument.getValue().get("key2")).isEqualTo("param");
  }

  @Test public void useDefaultValueOnTrackValueWhenItIsSet() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultResult = "defaultResult") void trackDefaultValue() {
      }
    }
    initMethod(Foo.class, "trackDefaultValue");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.<Integer>emptySet()));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultResult");
  }

  @Test public void useDefaultValueOnTrackValueWhenThereIsReturnValueAndItIsSet() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultResult = "defaultResult") String trackDefaultValueWithReturn() {
        return "returnValue";
      }
    }
    initMethod(Foo.class, "trackDefaultValueWithReturn");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.<Integer>emptySet()));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultResult");
  }

  @Test public void testFilters() throws Throwable {
    class Foo {
      @TrackFilter(TrackerType.MIXPANEL) @TrackEvent("title") void trackEventFilter() {
      }
    }
    initMethod(Foo.class, "trackEventFilter");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Set> filters = ArgumentCaptor.forClass(Set.class);

    verify(tracker).event(eq("title"), anyMap(), filters.capture());

    assertThat(filters.getValue()).containsExactly(TrackerType.MIXPANEL.getValue());
  }

  @Test public void testTrack() throws Throwable {
    class Foo {
      @Track(eventName = "Event", attributeKey = "key", attributeValue = "value") void trackWithTrack() {
      }
    }
    initMethod(Foo.class, "trackWithTrack");

    aspect.weaveJoinPointTrack(joinPoint);

    verify(tracker).event(eq("Event"), valueMapCaptor.capture(), eq(Collections.<Integer>emptySet()));
    assertThat(valueMapCaptor.getValue()).containsEntry("key", "value");
  }

  Method initMethod(Class<?> klass, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
    Method method = klass.getDeclaredMethod(name, parameterTypes);
    when(methodSignature.getMethod()).thenReturn(method);
    return method;
  }

}