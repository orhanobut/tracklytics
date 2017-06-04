package com.orhanobut.sample;

import com.orhanobut.tracklytics.Event;
import com.orhanobut.tracklytics.EventSubscriber;
import com.orhanobut.tracklytics.Tracklytics;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

// This is not an example of how to test
// The only purpose of this code is to see if the test byte code has the aspects
public class TrackingTest {

  private final Map<String, Event> triggeredEvents = new HashMap<>();

  @Before public void setup() {
    Tracklytics.init(new EventSubscriber() {
      @Override public void onEventTracked(Event event) {
        triggeredEvents.put(event.name, event);
      }
    });
  }

  @Test public void confirmKotlinAspects() {
    new FooKotlin().trackFoo();

    assertThat(triggeredEvents).containsKey("event_kotlin");
  }

  @Test public void confirmJavaAspects() {
    new Foo().trackFoo();

    assertThat(triggeredEvents).containsKey("event_java");
  }
}