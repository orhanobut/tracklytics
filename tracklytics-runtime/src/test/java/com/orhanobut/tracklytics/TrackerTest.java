package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventItem;
import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TrackerTest {

  @Mock TrackingAdapter trackingAdapter;
  @Mock TrackingAdapter trackingAdapter2;

  TrackingAdapter[] tools;
  Tracker tracker;

  @Before public void setup() {
    initMocks(this);
    tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    tracker = new Tracker.Default().init(tools);

    when(trackingAdapter.id()).thenReturn(100);
    when(trackingAdapter2.id()).thenReturn(200);
    when(trackingAdapter.toString()).thenReturn("Tracker");
  }

  @Test public void doNotTrackEventWhenDisabled() {
    tracker.enabled(false);
    tracker.event("title", null, null);

    assertThat(tracker.isEnabled()).isFalse();
    verify(trackingAdapter, never()).trackEvent("title", null, null);
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

  @Test public void doNotTrackEventWhenNotFiltered() {
    HashSet<Integer> filter = new HashSet<>();
    filter.add(200);

    tracker.event("title", null, null, filter);

    verify(trackingAdapter, never()).trackEvent("title", null, null);
  }

  @Test public void onlyFilteredTrackersShouldCallTrackEvent() {
    HashSet<Integer> filter = new HashSet<>();
    filter.add(100);

    tracker.event("title", null, null, filter);

    verify(trackingAdapter).trackEvent("title", null, null);
    verify(trackingAdapter2, never()).trackEvent("title", null, null);
  }

  @Test public void allTrackersShouldCallTrackEventWhenThereIsNoFilter() {
    tracker.event("title", null, null, Collections.<Integer>emptySet());

    verify(trackingAdapter).trackEvent("title", null, null);
    verify(trackingAdapter2).trackEvent("title", null, null);
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

    tracker.event("title", null, null);

    verify(subscriber, times(2)).onEventAdded(any(EventItem.class));
  }
}