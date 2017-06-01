package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TracklyticsTest {

  @Mock TrackingAdapter trackingAdapter;
  @Mock TrackingAdapter trackingAdapter2;
  @Mock TrackEvent trackEvent;

  TrackingAdapter[] tools;
  Tracklytics tracklytics;

  @Before public void setup() {
    initMocks(this);
    tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    tracklytics = Tracklytics.init(tools);

    when(trackingAdapter.id()).thenReturn(100);
    when(trackingAdapter2.id()).thenReturn(200);
    when(trackingAdapter.toString()).thenReturn("Tracklytics");

    when(trackEvent.value()).thenReturn("event");
    when(trackEvent.filters()).thenReturn(new int[]{1, 2});
  }

  @Test public void doNotTrackEventWhenDisabled() {
    tracklytics.enabled(false);
    tracklytics.event(trackEvent, null, null);

    assertThat(tracklytics.isEnabled()).isFalse();

    verifyZeroInteractions(trackingAdapter, trackingAdapter2);
  }

  @Test public void isEnabledShouldReturnTrueAsDefault() {
    assertThat(tracklytics.isEnabled()).isTrue();
  }

  @Test public void startShouldInvokeStartForeachTrackingTool() {
    tracklytics.start();

    verify(tools[0]).start();
    verify(tools[1]).start();
  }

  @Test public void stopShouldInvokeStopForeachTrackingTool() {
    tracklytics.stop();

    verify(tools[0]).stop();
    verify(tools[1]).stop();
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
}