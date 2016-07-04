package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.orhanobut.tracklytics.AssertTracker.assertTrack;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings("ALL")
public class TrackerAspectTest {
  TrackerAspect aspect;

  Tracker tracker;

  @Mock ProceedingJoinPoint joinPoint;
  @Mock MethodSignature methodSignature;
  @Mock TrackingAdapter trackingAdapter;

  @Before public void setup() throws Exception {
    initMocks(this);

    tracker = spy(Tracker.init(trackingAdapter));
    aspect = new TrackerAspect();
    aspect.init(tracker);

    when(joinPoint.getSignature()).thenReturn(methodSignature);
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

  @Ignore("Add a test to make sure it works")
  @Test public void testInit() {
  }

  @Test public void testStart() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.START) public void start() {
      }
    }
    initMethod(Foo.class, "start");

    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);
    verify(tracker).start();
  }

  @Test public void testStop() throws Throwable {
    class Foo {
      @Tracklytics(TrackerAction.STOP) public void stop() {
      }
    }
    initMethod(Foo.class, "stop");
    Tracker tracker = (Tracker) aspect.weaveJointTracklytics(joinPoint);

    verify(tracker).stop();
  }

  @Test public void trackEventWithoutAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .noAttributes()
        .noSuperAttributes();
  }

  @Test public void useReturnValueAsAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key") public String foo() {
        return "test";
      }
    }

    when(joinPoint.proceed()).thenReturn("test");
    invokeMethod(Foo.class, "foo");

    assertTrack(tracker)
        .event("title")
        .noTags()
        .noFilters()
        .attribute("key", "test")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "test")
        .attribute("key2", "param")
        .noSuperAttributes();
  }

  @Test public void useDefaultValueWhenThereIsNoReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaultValue") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "defaultValue")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "returnValue")
        .noSuperAttributes();
  }

  @Test public void useDefaultValueWhenParameterValueIsNull() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo(@Attribute(value = "key1", defaultValue = "default") String val) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{null});
    invokeMethod(Foo.class, "foo", String.class);

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "default")
        .noSuperAttributes();
  }

  @Test public void testFixedAttributeOnMethodScope() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttribute(key = "key1", value = "value") public String foo() {
        return "returnValue";
      }
    }
    invokeMethod(Foo.class, "foo");

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .attribute("key3", "value3")
        .attribute("key4", "value4")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .attribute("key3", "value3")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .superAttribute("key1", "value1")
        .superAttribute("key2", "value2");

    invokeMethod(Foo.class, "foo2");
    assertTrack(tracker)
        .event("event2")
        .noFilters()
        .noAttributes()
        .superAttribute("key1", "value1")
        .superAttribute("key2", "value2");
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

    when(joinPoint.proceed()).thenReturn("value1");
    invokeMethod(Foo.class, "foo");

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .attribute("key3", "value3")
        .superAttribute("key2", "value2")
        .superAttribute("key3", "value3");

    invokeMethod(Foo.class, "foo2");

    assertTrack(tracker)
        .event("event2")
        .noFilters()
        .noTags()
        .noAttributes()
        .superAttribute("key2", "value2")
        .superAttribute("key3", "value3");
  }

  @Test public void testTrackable() throws Throwable {
    class Bar implements Trackable {

      @Override public Map<String, Object> getTrackableAttributes() {
        Map<String, Object> values = new HashMap<>();
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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .noSuperAttributes();
  }

  @Test public void ignoreNullValuesOnTrackable() throws Throwable {
    class Bar implements Trackable {

      @Override public Map<String, Object> getTrackableAttributes() {
        return null;
      }
    }

    class Foo {
      @TrackEvent("title") public void foo(@TrackableAttribute Bar bar) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{new Bar()});

    invokeMethod(Foo.class, "foo", Bar.class);

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .noAttributes()
        .noSuperAttributes();
  }

  @Test public void throwExceptionWhenTrackableAnnotationNotMatchWithValue() throws Throwable {

    class Foo {
      @TrackEvent("title") public void foo(@TrackableAttribute String bar) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{"sdfsd"});

    try {
      invokeMethod(Foo.class, "foo", String.class);

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

    assertTrack(tracker)
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "value2")
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .superAttribute("key1", "value1")
        .superAttribute("key2", "value2");
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "default1")
        .attribute("key2", "default2")
        .noSuperAttributes();
  }

  @Test public void testTrackableAttributeForCurrentClass() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, Object> getTrackableAttributes() {
        Map<String, Object> map = new HashMap<>();
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key", "value")
        .noSuperAttributes();
  }

  @Test public void doNotUseTrackableAttributesWhenTrackableAttributeNotExists() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, Object> getTrackableAttributes() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        return map;
      }

      @TrackEvent("event")
      public void foo() {
      }
    }

    when(joinPoint.getThis()).thenReturn(new Foo());
    invokeMethod(Foo.class, "foo");

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .noAttributes()
        .noSuperAttributes();
  }

  @Test public void ignoreNullValueOnTrackableAttributeForCurrentClass() throws Throwable {
    class Foo implements Trackable {

      @Override public Map<String, Object> getTrackableAttributes() {
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .noAttributes()
        .noSuperAttributes();
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key", "method")
        .attribute("key1", "method1")
        .noSuperAttributes();
  }

  @Test public void useThisClassWhenCalledFromSuperClass() throws Throwable {
    @FixedAttribute(key = "key0", value = "value0")
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

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key0", "value0")
        .attribute("key", "value")
        .attribute("key2", "value2")
        .noSuperAttributes();
  }

  @Ignore("It doesn't work on CI, need to find the reason")
  @Test public void testScreenNameAttribute() throws Throwable {
    @ScreenNameAttribute(key = "name", excludeLast = 2, delimiter = "-")
    class BasePresenter {

      @TrackEvent("event")
      public void base() {
      }
    }

    class FooBarBasePresenter extends BasePresenter {
    }

    initMethod(FooBarBasePresenter.class, "base");
    when(joinPoint.getThis()).thenReturn(new FooBarBasePresenter());
    aspect.weaveJoinPointTrackEvent(joinPoint);

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("name", "Foo-Bar")
        .noSuperAttributes();
  }

  @Test public void subclassClassAttributeShouldOverrideScreenNameAttribute() throws Throwable {
    @ScreenNameAttribute(key = "key", excludeLast = 2, delimiter = "-")
    class BasePresenter {

      @TrackEvent("event")
      public void base() {
      }
    }

    @FixedAttribute(key = "key", value = "value1")
    class FooBarBasePresenter extends BasePresenter {
    }

    initMethod(FooBarBasePresenter.class, "base");
    when(joinPoint.getThis()).thenReturn(new FooBarBasePresenter());
    aspect.weaveJoinPointTrackEvent(joinPoint);

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key", "value1")
        .noSuperAttributes();
  }

  @Test public void testLog() throws Throwable {
    TracklyticsLogger logger = mock(TracklyticsLogger.class);
    tracker.setLogger(logger);

    class Foo {
      @TrackEvent("event")
      @FixedAttribute(key = "key", value = "value")
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    verify(logger).log(contains("] event-> {key=value}, super attrs: {}, filters: []"));
  }

  @Test public void testFilters() throws Throwable {
    class Foo {
      @TrackEvent(value = "event", filters = {100, 200})
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    int[] tags = {100, 200};

    assertTrack(tracker)
        .event("event")
        .noTags()
        .filters(100, 200)
        .noAttributes()
        .noSuperAttributes();
  }

  @Test public void testTagss() throws Throwable {
    class Foo {
      @TrackEvent(value = "event", tags = {"abc", "123"})
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    int[] tags = {100, 200};

    assertTrack(tracker)
        .event("event")
        .noFilters()
        .tags("abc", "123")
        .noAttributes()
        .noSuperAttributes();
  }
}