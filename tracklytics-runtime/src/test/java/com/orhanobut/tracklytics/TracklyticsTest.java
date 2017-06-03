package com.orhanobut.tracklytics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TracklyticsTest {

  @Mock EventSubscriber eventSubscriber;
  @Mock TrackEvent trackEvent;

  private Tracklytics tracklytics;

  @Before public void setup() {
    initMocks(this);

    tracklytics = Tracklytics.init(eventSubscriber);

    when(eventSubscriber.toString()).thenReturn("Tracklytics");

    when(trackEvent.value()).thenReturn("event");
    when(trackEvent.filters()).thenReturn(new int[]{1, 2});
  }

  @Test public void addSuperAttributeWithoutAnnotation() {
    tracklytics.addSuperAttribute("key", "value");

    assertThat(tracklytics.superAttributes).containsEntry("key", "value");
  }

  @Test public void removeSuperAttributeWithoutAnnotation() {
    tracklytics.superAttributes.put("key", "value");
    tracklytics.removeSuperAttribute("key");

    assertThat(tracklytics.superAttributes).doesNotContainKey("key");
  }

  @Test public void trackWithoutAnnotation() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("key", "value");

    tracklytics.trackEvent("event_name", attributes);

    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

    verify(eventSubscriber).onEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).containsEntry("key", "value");
  }

  @Test public void trackWithEvent() {
    tracklytics.trackEvent("event_name");

    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

    verify(eventSubscriber).onEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue().name).isEqualTo("event_name");
    assertThat(eventCaptor.getValue().attributes).isNull();
  }
}