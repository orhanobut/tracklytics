package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventItem;
import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TrackerTest {

  @Mock TrackingAdapter trackingAdapter;
  @Mock TrackingAdapter trackingAdapter2;
  @Mock TrackEvent trackEvent;

  TrackingAdapter[] tools;
  Tracker tracker;

  @Before public void setup() {
    initMocks(this);
    tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    tracker = Tracker.init(tools);

    when(trackingAdapter.id()).thenReturn(100);
    when(trackingAdapter2.id()).thenReturn(200);
    when(trackingAdapter.toString()).thenReturn("Tracker");

    when(trackEvent.value()).thenReturn("event");
    when(trackEvent.tags()).thenReturn(new int[]{1, 2});
  }

  @Test public void doNotTrackEventWhenDisabled() {
    tracker.enabled(false);
    tracker.event(trackEvent, null, null);

    assertThat(tracker.isEnabled()).isFalse();

    verifyZeroInteractions(trackingAdapter, trackingAdapter2);
  }

  @Test public void isEnabledShouldReturnTrueAsDefault() {
    assertThat(tracker.isEnabled()).isTrue();
  }

  @Test public void startShouldInvokeStartForeachTrackingTool() {
    tracker.start();

    verify(tools[0]).start();
    verify(tools[1]).start();
  }

  @Test public void stopShouldInvokeStopForeachTrackingTool() {
    tracker.stop();

    verify(tools[0]).stop();
    verify(tools[1]).stop();
  }

  @Test public void trackEventShouldInvokeEventQueue() {
    class Subscriber implements TrackEventSubscriber {
      EventItem item;

      @Override public void onEventAdded(EventItem item) {
        this.item = item;
      }
    }

    Subscriber subscriber = spy(new Subscriber());
    EventQueue.subscribe(subscriber);

    tracker.event(trackEvent, null, null);

    verify(subscriber, times(2)).onEventAdded(any(EventItem.class));
  }
}