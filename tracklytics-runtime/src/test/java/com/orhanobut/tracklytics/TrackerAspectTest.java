package com.orhanobut.tracklytics;

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
import static org.mockito.Mockito.mock;
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

    tracker = spy(new Tracker.Default().init(trackingAdapter));
    aspect.init(tracker);
  }

  Method initMethod(Class<?> klass, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
    Method method = klass.getDeclaredMethod(name, parameterTypes);
    when(methodSignature.getMethod()).thenReturn(method);
    return method;
  }

  @Test public void testInit() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.INIT) Tracker init() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "init");

    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);

    assertThat(tracker).isEqualTo(tracker);
  }

  @Test public void testStart() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.START) Tracker start() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "start");

    aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).start();
  }

  @Test public void testStop() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.STOP) Tracker stop() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "stop");

    aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).stop();
  }

  @Test public void trackEventWithoutAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") void noAttribute() {
      }
    }
    initMethod(Foo.class, "noAttribute");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(argument.getValue()).isEmpty();
  }

  @Test public void useReturnValueAsAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key") String foo() {
        return "test";
      }
    }

    initMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("test");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> values = ArgumentCaptor.forClass(Map.class);
    verify(tracker).event(eq("title"), values.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(values.getValue()).containsEntry("key", "test");
  }

  @Test public void useReturnValueAndParametersAsAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key1") String foo(@Attribute("key2") String param) {
        return "test";
      }
    }
    initMethod(Foo.class, "foo", String.class);
    when(joinPoint.proceed()).thenReturn("test");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(argument.getValue()).containsOnlyKeys("key1", "key2");
    assertThat(argument.getValue().get("key1")).isEqualTo("test");
    assertThat(argument.getValue().get("key2")).isEqualTo("param");
  }

  @Test public void useDefaultValueWhenThereIsNoReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaultValue") void foo() {
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultValue");
  }

  @Test public void useReturnValueWhenItIsNotNull() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaulValue") String foo() {
        return "returnValue";
      }
    }
    initMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("returnValue");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "returnValue");
  }

  @Test public void useDefaultValueWhenReturnValueIsNull() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaulValue") String foo() {
        return null;
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaulValue");
  }

  @Test public void testFixedAttributeOnMethodScope() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttribute(key = "key1", value = "value") String foo() {
        return "returnValue";
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value");
  }

  @Test public void testFixedAttributeOnClassScope() throws Throwable {
    @FixedAttributes({
        @FixedAttribute(key = "key1", value = "value1"),
        @FixedAttribute(key = "key2", value = "value2")
    })
    @FixedAttribute(key = "key3", value = "value3")
    class Foo {
      @TrackEvent("title")
      @FixedAttribute(key = "key4", value = "value4")
      public void foo() {
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getValue()).containsEntry("key3", "value3");
    assertThat(valueMapCaptor.getValue()).containsEntry("key4", "value4");
  }

  @Test public void testFixedAttributeAndAttributeAtSameTime() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute("key1")
      @FixedAttribute(key = "key2", value = "value2")
      public String foo() {
        return "value1";
      }
    }
    initMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("value1");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
  }

  @Test public void testFixedAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttributes({
          @FixedAttribute(key = "key1", value = "value1"),
          @FixedAttribute(key = "key2", value = "value2")
      })
      @FixedAttribute(key = "key3", value = "value3")
      public void foo() {
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getValue()).containsEntry("key3", "value3");
  }

  @Test public void testSuperAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", isSuper = true)
      public String foo() {
        return "value1";
      }

      @TrackEvent("event2")
      public void foo2() {
      }
    }

    initMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("value1");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key1", "value1");

    initMethod(Foo.class, "foo2");
    aspect.weaveJoinPointTrackEvent(joinPoint);
    verify(tracker).event(eq("event2"), anyMap(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
  }

  @Test public void testSuperFixedAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttributes({
          @FixedAttribute(key = "key1", value = "value1"),
          @FixedAttribute(key = "key2", value = "value2", isSuper = true)
      })
      @FixedAttribute(key = "key3", value = "value3", isSuper = true)
      public String foo() {
        return "returnValue";
      }

      @TrackEvent("event2")
      public void foo2() {
      }
    }

    initMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("value1");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key3", "value3");

    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key3", "value3");

    initMethod(Foo.class, "foo2");
    aspect.weaveJoinPointTrackEvent(joinPoint);
    verify(tracker).event(eq("event2"), anyMap(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getValue()).containsEntry("key3", "value3");
    assertThat(valueMapCaptor.getValue()).doesNotContainEntry("key1", "value1");
  }

  @Test public void testFilters() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    when(trackingAdapter.id()).thenReturn(1);

    class Foo {
      @TrackFilter(1) @TrackEvent("title") void foo() {
      }
    }
    initMethod(Foo.class, "foo");

    aspect.weaveJoinPointTrackEvent(joinPoint);

    ArgumentCaptor<Set> filters = ArgumentCaptor.forClass(Set.class);

    verify(tracker).event(eq("title"), anyMap(), anyMap(), filters.capture());

    assertThat(filters.getValue()).containsExactly(1);
  }

}