package com.orhanobut.tracklytics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TracklyticsTest {

  @Mock EventSubscriber eventSubscriber;
  @Mock TrackEvent trackEvent;
  @Captor ArgumentCaptor<Event> eventCaptor;

  private Tracklytics tracklytics;

  @Before public void setup() {
    initMocks(this);

    tracklytics = Tracklytics.init(eventSubscriber);

    when(eventSubscriber.toString()).thenReturn("Tracklytics");

    when(trackEvent.value()).thenReturn("event");
    when(trackEvent.filters()).thenReturn(new int[]{1, 2});
  }

  @Test public void trackWithoutAnnotation() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key", "value");

    tracklytics.trackEvent("event_name", attributes);

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).containsExactly("key", "value");
  }

  @Test public void trackFromAspectEvent() throws Throwable {
    class Foo {
      @TrackEvent("event_name") public void foo() {
      }
    }
    TrackEvent trackEvent = Foo.class.getMethod("foo").getAnnotation(TrackEvent.class);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key", "value");

    tracklytics.onAspectEventTriggered(trackEvent, attributes);

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).containsExactly("key", "value");
  }

  @Test public void trackWithEvent() {
    tracklytics.trackEvent("event_name");

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).isNull();
  }

  @Test public void addSuperAttributesToEvent() {
    tracklytics.addSuperAttribute("key1", "value1");
    tracklytics.addSuperAttribute("key2", "value2");

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key3", "value3");

    tracklytics.trackEvent("event_name", attributes);

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).containsExactly("key3", "value3");
    assertThat(eventCaptor.getValue().superAttributes).containsExactly("key1", "value1", "key2", "value2");
  }

  @Test public void addSuperAttributeFromAspects() {
    tracklytics.onAspectSuperAttributeAdded("key1", "value1");

    tracklytics.trackEvent("event_name");

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().superAttributes).containsExactly("key1", "value1");
  }

  @Test public void removeSuperAttributes() {
    tracklytics.addSuperAttribute("key1", "value1");
    tracklytics.addSuperAttribute("key2", "value2");
    tracklytics.addSuperAttribute("key3", "value3");

    tracklytics.removeSuperAttribute("key1");
    tracklytics.removeSuperAttribute("key2");

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key4", "value4");

    tracklytics.trackEvent("event_name", attributes);

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).containsExactly("key4", "value4");
    assertThat(eventCaptor.getValue().superAttributes).containsExactly("key3", "value3");
  }

  @Test public void removeSuperAttributeFromAspects() {
    tracklytics.addSuperAttribute("key1", "value1");
    tracklytics.addSuperAttribute("key2", "value2");

    tracklytics.onAspectSuperAttributeRemoved("key1");

    tracklytics.trackEvent("event_name");

    verify(eventSubscriber).onEventTracked(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().superAttributes).containsExactly("key2", "value2");
  }

  @Test public void log() throws Throwable {
    EventLogListener logger = mock(EventLogListener.class);
    tracklytics.setEventLogListener(logger);

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key", "value");

    tracklytics.trackEvent("event", attributes);

    verify(logger).log("event-> {key=value}, super attrs: {}, filters: null");
  }
}