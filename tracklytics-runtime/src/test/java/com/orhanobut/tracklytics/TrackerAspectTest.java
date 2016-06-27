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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings("ALL")
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

  private Method invokeMethod(Class<?> klass, String methodName, Class<?>... parameterTypes) throws Throwable {
    Method method = initMethod(klass, methodName, parameterTypes);
    Object instance = new Object();
    when(joinPoint.getThis()).thenReturn(instance);

    aspect.weaveJoinPointTrackEvent(joinPoint);
    return method;
  }

  private Method initMethod(Class<?> klass, String name, Class<?>... parameterTypes) throws Throwable {
    Method method = klass.getMethod(name, parameterTypes);
    when(methodSignature.getMethod()).thenReturn(method);
    return method;
  }

  @Test public void testInit() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.INIT) public Tracker init() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "init");

    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);

    assertThat(tracker).isEqualTo(tracker);
  }

  @Test public void testStart() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.START) public Tracker start() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "start");

    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);
    verify(tracker).start();
  }

  @Test public void testStop() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.STOP) public Tracker stop() {
        return new Tracker.Default();
      }
    }
    initMethod(Foo.class, "stop");
    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).stop();
  }

  @Test public void trackEventWithoutAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") public void noAttribute() {
      }
    }
    invokeMethod(Foo.class, "noAttribute");

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(argument.getValue()).isEmpty();
  }

  @Test public void useReturnValueAsAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key") public String foo() {
        return "test";
      }
    }

    when(joinPoint.proceed()).thenReturn("test");
    invokeMethod(Foo.class, "foo");

    ArgumentCaptor<Map> values = ArgumentCaptor.forClass(Map.class);
    verify(tracker).event(eq("title"), values.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(values.getValue()).containsEntry("key", "test");
  }

  @Test public void useReturnValueAndParametersAsAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key1") public String foo(@Attribute("key2") String param) {
        return "test";
      }
    }

    when(joinPoint.proceed()).thenReturn("test");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});
    invokeMethod(Foo.class, "foo", String.class);

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    verify(tracker).event(eq("title"), argument.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(argument.getValue()).containsOnlyKeys("key1", "key2");
    assertThat(argument.getValue().get("key1")).isEqualTo("test");
    assertThat(argument.getValue().get("key2")).isEqualTo("param");
  }

  @Test public void useDefaultValueWhenThereIsNoReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaultValue") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "defaultValue");
  }

  @Test public void useReturnValueWhenItIsNotNull() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaulValue") public String foo() {
        return "returnValue";
      }
    }
    when(joinPoint.proceed()).thenReturn("returnValue");
    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "returnValue");
  }

  @Test public void useDefaultValueWhenParameterValueIsNull() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo(@Attribute(value = "key1", defaultValue = "default") String val) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{null});
    invokeMethod(Foo.class, "foo", String.class);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "default");
  }

  @Test public void testFixedAttributeOnMethodScope() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttribute(key = "key1", value = "value") public String foo() {
        return "returnValue";
      }
    }
    invokeMethod(Foo.class, "foo");

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
    invokeMethod(Foo.class, "foo");

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

    when(joinPoint.proceed()).thenReturn("value1");
    invokeMethod(Foo.class, "foo");

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
    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));

    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getValue()).containsEntry("key3", "value3");
  }

  @Test public void testSuperAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", isSuper = true)
      public String foo(@Attribute(value = "key2", isSuper = true) String value) {
        return "value1";
      }

      @TrackEvent("event2")
      public void foo2() {
      }
    }

    when(joinPoint.proceed()).thenReturn("value1");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"value2"});
    invokeMethod(Foo.class, "foo", String.class);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key2", "value2");

    invokeMethod(Foo.class, "foo2");
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

    invokeMethod(Foo.class, "foo");
    when(joinPoint.proceed()).thenReturn("value1");

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key3", "value3");

    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key3", "value3");

    invokeMethod(Foo.class, "foo2");
    verify(tracker).event(eq("event2"), anyMap(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getValue()).containsEntry("key3", "value3");
    assertThat(valueMapCaptor.getValue()).doesNotContainEntry("key1", "value1");
  }

  @Test public void testFilters() throws Throwable {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    when(trackingAdapter.id()).thenReturn(1);

    class Foo {
      @TrackFilter(1) @TrackEvent("title") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    ArgumentCaptor<Set> filters = ArgumentCaptor.forClass(Set.class);

    verify(tracker).event(eq("title"), anyMap(), anyMap(), filters.capture());

    assertThat(filters.getValue()).containsExactly(1);
  }

  @Test public void testTrackable() throws Throwable {
    class Bar implements Trackable {

      @Override public Map<String, String> getTrackableAttributes() {
        Map<String, String> values = new HashMap<>();
        values.put("key1", "value1");
        values.put("key2", "value2");
        return values;
      }
    }

    class Foo {
      @TrackEvent("title") public void foo(@TrackableAttribute Bar bar) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{new Bar()});

    invokeMethod(Foo.class, "foo", Bar.class);

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsKeys("key1", "key2");
  }

  @Test public void ignoreNullValuesOnTrackable() throws Throwable {
    class Bar implements Trackable {

      @Override public Map<String, String> getTrackableAttributes() {
        return null;
      }
    }

    class Foo {
      @TrackEvent("title") public void foo(@TrackableAttribute Bar bar) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{new Bar()});

    invokeMethod(Foo.class, "foo", Bar.class);

    verify(tracker).event(eq("title"), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
  }

  @Test public void throwExceptionWhenTrackableAnnotationNotMatchWithValue() throws Throwable {

    class Foo {
      @TrackEvent("title") public void foo(@TrackableAttribute String bar) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{"sdfsd"});

    initMethod(Foo.class, "foo", String.class);
    Object instance = new Object();
    when(joinPoint.getThis()).thenReturn(instance);

    try {
      aspect.weaveJoinPointTrackEvent(joinPoint);
      fail("Should throw exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Trackable interface must be implemented for the parameter type");
    }
  }

  @Test public void testMethodParameterWithoutAnnotation() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo(@Attribute("Key") String bar, String param2) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{"sdfsd"});

    invokeMethod(Foo.class, "foo", String.class, String.class);

    try {
      aspect.weaveJoinPointTrackEvent(joinPoint);
    } catch (Exception e) {
      fail("Method parameters without annotation should be accepted");
    }
  }

  @Test public void testClassWideAttributeInAnonymousClass() throws Throwable {
    @FixedAttribute(key = "key1", value = "value1")
    class Foo {

      @FixedAttribute(key = "key2", value = "value2")
      class Inner {

        @TrackEvent("title")
        public void bar() {
        }
      }
    }

    invokeMethod(Foo.Inner.class, "bar");

    verify(tracker).event(eq("title"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
  }

  @Test public void testTransformAttributeForParameters() throws Throwable {
    class Foo {
      @TrackEvent("event")
      @TransformAttributeMap(
          keys = {0, 1},
          values = {"value1", "value2"}
      )
      public void foo(@TransformAttribute("key1") Integer type) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{0});
    invokeMethod(Foo.class, "foo", Integer.class);

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value1");
  }

  @Test public void testTransformAttributeMapInvalidState() throws Throwable {
    class Foo {
      @TrackEvent("event")
      @TransformAttributeMap(
          keys = {0, 1},
          values = {"value1"}
      )
      public void foo(@TransformAttribute("key1") Integer type) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{0});

    try {
      invokeMethod(Foo.class, "foo", Integer.class);
    } catch (Exception e) {
      assertThat(e).hasMessage("TransformAttributeMap keys and values must have same length");
    }
  }

  @Test public void testTransformAttributeWithoutTransformAttributeMap() throws Throwable {
    class Foo {
      @TrackEvent("event")
      public void foo(@TransformAttribute("key1") Integer type) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{0});

    try {
      invokeMethod(Foo.class, "foo", Integer.class);
    } catch (Exception e) {
      assertThat(e).hasMessage("Method must have TransformAttributeMap when TransformAttribute is used");
    }
  }

  @Test public void testTransformAttributeForReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("event")
      @TransformAttributeMap(
          keys = {0, 1},
          values = {"value1", "value2"}
      )
      @TransformAttribute("key1")
      public int foo() {
        return 1;
      }
    }

    when(joinPoint.proceed()).thenReturn(1);
    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "value2");
  }

  @Test public void testSuperTransformAttribute() throws Throwable {
    class Foo {
      @TrackEvent("event")
      @TransformAttributeMap(
          keys = {0, 1},
          values = {"value1", "value2"}
      )
      @TransformAttribute(value = "key1", isSuper = true)
      public int foo(@TransformAttribute(value = "key2", isSuper = true) Integer val) {
        return 0;
      }
    }

    when(joinPoint.proceed()).thenReturn(0);
    when(joinPoint.getArgs()).thenReturn(new Object[]{1});
    invokeMethod(Foo.class, "foo", Integer.class);

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), valueMapCaptor.capture(), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(0)).containsEntry("key2", "value2");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key1", "value1");
    assertThat(valueMapCaptor.getAllValues().get(1)).containsEntry("key2", "value2");
  }

  @Test public void testTransformAttributeDefaultValue() throws Throwable {
    class Foo {
      @TrackEvent("event")
      @TransformAttributeMap(
          keys = {0, 1},
          values = {"value1", "value2"}
      )
      @TransformAttribute(value = "key1", defaultValue = "default1")
      public String foo(@TransformAttribute(value = "key2", defaultValue = "default2") Integer val) {
        return null;
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{null});
    invokeMethod(Foo.class, "foo", Integer.class);

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "default1");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "default2");
  }

  @Test public void testTrackableAttributeForCurrentClass() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, String> getTrackableAttributes() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        return map;
      }

      @TrackEvent("event")
      @TrackableAttribute
      public void foo() {
      }
    }

    initMethod(Foo.class, "foo");
    when(joinPoint.getThis()).thenReturn(new Foo());
    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key", "value");
  }

  @Test public void doNotUseTrackableAttributesWhenTrackableAttributeNotExists() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, String> getTrackableAttributes() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        return map;
      }

      @TrackEvent("event")
      public void foo() {
      }
    }

    when(joinPoint.getThis()).thenReturn(new Foo());
    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).doesNotContainEntry("key", "value");
  }

  @Test public void ignoreNullValueOnTrackableAttributeForCurrentClass() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, String> getTrackableAttributes() {
        return null;
      }

      @TrackEvent("event")
      @TrackableAttribute
      public void foo() {
      }
    }

    initMethod(Foo.class, "foo");
    when(joinPoint.getThis()).thenReturn(new Foo());
    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("event"), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
  }

  @Test public void overrideClassWideAttributeOnMethodWhenAttributesAreSame() throws Throwable {
    @FixedAttribute(key = "key", value = "class")
    @FixedAttributes(
        @FixedAttribute(key = "key1", value = "class1")
    )
    class Foo {

      @TrackEvent("event")
      @FixedAttribute(key = "key", value = "method")
      @FixedAttributes(
          @FixedAttribute(key = "key1", value = "method1")
      )
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key", "method");
    assertThat(valueMapCaptor.getValue()).containsEntry("key1", "method1");
  }

  @Test public void useThisClassWhenCalledFromSuperClass() throws Throwable {
    class Base {

      @TrackEvent("event")
      public void base() {
      }
    }

    @FixedAttribute(key = "key", value = "value")
    @FixedAttributes(
        @FixedAttribute(key = "key2", value = "value2")
    )
    class Foo extends Base {
    }

    initMethod(Foo.class, "base");
    when(joinPoint.getThis()).thenReturn(new Foo());
    aspect.weaveJoinPointTrackEvent(joinPoint);

    verify(tracker).event(eq("event"), valueMapCaptor.capture(), eq(Collections.EMPTY_MAP), eq(Collections.EMPTY_SET));
    assertThat(valueMapCaptor.getValue()).containsEntry("key", "value");
    assertThat(valueMapCaptor.getValue()).containsEntry("key2", "value2");
  }
}