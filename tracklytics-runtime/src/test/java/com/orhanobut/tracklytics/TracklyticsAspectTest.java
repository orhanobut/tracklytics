package com.orhanobut.tracklytics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings("ALL")
public class TracklyticsAspectTest {

  @Mock ProceedingJoinPoint joinPoint;
  @Mock MethodSignature methodSignature;

  private final Map<String, Object> superAttributes = new HashMap<>();

  private TracklyticsAspect aspect;
  private TrackEvent trackEvent;
  private Map<String, Object> attributes;
  private AspectListener aspectListener;

  @Before public void setup() throws Exception {
    initMocks(this);

    aspectListener = new AspectListener() {
      @Override public void onAspectEventTriggered(TrackEvent trackEvent, Map<String, Object> attributes) {
        TracklyticsAspectTest.this.trackEvent = trackEvent;
        TracklyticsAspectTest.this.attributes = attributes;
      }

      @Override public void onAspectSuperAttributeAdded(String key, Object value) {
        superAttributes.put(key, value);
      }

      @Override public void onAspectSuperAttributeRemoved(String key) {
        superAttributes.remove(key);
      }
    };

    aspect = new TracklyticsAspect();
    aspect.subscribe(aspectListener);

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

  @Test public void trackEventWithoutAttributes() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .noAttributes();
  }

  @Test public void useReturnValueAsAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title") @Attribute("key") public String foo() {
        return "test";
      }
    }

    when(joinPoint.proceed()).thenReturn("test");
    invokeMethod(Foo.class, "foo");

    assertTrack()
        .event("title")
        .noTags()
        .noFilters()
        .attribute("key", "test");
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "test")
        .attribute("key2", "param");
  }

  @Test public void useDefaultValueWhenThereIsNoReturnValue() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", defaultValue = "defaultValue") public void foo() {
      }
    }
    invokeMethod(Foo.class, "foo");

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "defaultValue");
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "returnValue");
  }

  @Test public void useDefaultValueWhenParameterValueIsNull() throws Throwable {
    class Foo {
      @TrackEvent("title") public void foo(@Attribute(value = "key1", defaultValue = "default") String val) {
      }
    }

    when(joinPoint.getArgs()).thenReturn(new Object[]{null});
    invokeMethod(Foo.class, "foo", String.class);

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "default");
  }

  @Test public void fixedAttributeOnMethodScope() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @FixedAttribute(key = "key1", value = "value") public String foo() {
        return "returnValue";
      }
    }
    invokeMethod(Foo.class, "foo");

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value");
  }

  @Test public void fixedAttributeOnClassScope() throws Throwable {
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .attribute("key3", "value3")
        .attribute("key4", "value4");
  }

  @Test public void fixedAttributeAndAttributeAtSameTime() throws Throwable {
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2");
  }

  @Test public void fixedAttributes() throws Throwable {
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2")
        .attribute("key3", "value3");
  }

  @Test public void superAttribute() throws Throwable {
    class Foo {
      @TrackEvent("title")
      @Attribute(value = "key1", isSuper = true)
      public String foo(@Attribute(value = "key2", isSuper = true) String value) {
        return "value1";
      }
    }

    when(joinPoint.proceed()).thenReturn("value1");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"value2"});

    invokeMethod(Foo.class, "foo", String.class);

    assertThat(superAttributes).containsExactly("key1", "value1", "key2", "value2");
  }

  @Test public void superFixedAttribute() throws Throwable {
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
    }

    when(joinPoint.proceed()).thenReturn("value1");
    invokeMethod(Foo.class, "foo");

    assertThat(superAttributes).containsExactly("key2", "value2", "key3", "value3");
  }

  @Test public void superTransformAttribute() throws Throwable {
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

    assertThat(superAttributes).containsExactly("key1", "value1", "key2", "value2");
  }

  @Test public void trackable() throws Throwable {
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2");
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .noAttributes();
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

  @Test public void methodParameterWithoutAnnotation() throws Throwable {
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

  @Test public void classWideAttributeInAnonymousClass() throws Throwable {
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

    assertTrack()
        .event("title")
        .noFilters()
        .noTags()
        .attribute("key1", "value1")
        .attribute("key2", "value2");
  }

  @Test public void transformAttributeForParameters() throws Throwable {
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "value1");
  }

  @Test public void transformAttributeMapInvalidState() throws Throwable {
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

  @Test public void transformAttributeWithoutTransformAttributeMap() throws Throwable {
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

  @Test public void transformAttributeForReturnValue() throws Throwable {
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "value2");
  }

  @Test public void transformAttributeDefaultValue() throws Throwable {
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key1", "default1")
        .attribute("key2", "default2");
  }

  @Test public void trackableAttributeForCurrentClass() throws Throwable {
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key", "value");
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .noAttributes();
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .noAttributes();
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key", "method")
        .attribute("key1", "method1");
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

    assertTrack()
        .event("event")
        .noFilters()
        .noTags()
        .attribute("key0", "value0")
        .attribute("key", "value")
        .attribute("key2", "value2");
  }

  @Test public void filters() throws Throwable {
    class Foo {
      @TrackEvent(value = "event", filters = {100, 200})
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    int[] tags = {100, 200};

    assertTrack()
        .event("event")
        .noTags()
        .filters(100, 200)
        .noAttributes();
  }

  @Test public void tags() throws Throwable {
    class Foo {
      @TrackEvent(value = "event", tags = {"abc", "123"})
      public void foo() {
      }
    }

    invokeMethod(Foo.class, "foo");

    int[] tags = {100, 200};

    assertTrack()
        .event("event")
        .noFilters()
        .tags("abc", "123")
        .noAttributes();
  }

  AssertTracker assertTrack() {
    return new AssertTracker(trackEvent, attributes);
  }

}
