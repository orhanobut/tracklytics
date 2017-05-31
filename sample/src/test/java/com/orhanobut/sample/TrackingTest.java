package com.orhanobut.sample;

import com.orhanobut.tracklytics.Event;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.trackers.SimpleTrackingAdapter;

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
    Tracker.init(new SimpleTrackingAdapter() {
      @Override public void trackEvent(Event event, Map<String, Object> superAttributes) {
        super.trackEvent(event, superAttributes);

        triggeredEvents.put(event.eventName, event);
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