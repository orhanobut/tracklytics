package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventItem;
import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackerType;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TrackerTest {

  @Mock TrackingAdapter trackingAdapter;

  TrackingAdapter[] tools;
  Tracker tracker;

  @Before public void setup() {
    initMocks(this);
    tools = new TrackingAdapter[]{trackingAdapter};
    tracker = Tracker.init(tools);
  }

  @Test public void doNotTrackEventWhenDisabled() {
    tracker.enabled(false);
    tracker.event("title", null);

    assertThat(tracker.isEnabled()).isFalse();
    verify(trackingAdapter, never()).trackEvent("title", null);
  }

  @Test public void isEnabledShouldReturnTrueAsDefault() {
    assertThat(tracker.isEnabled()).isTrue();
  }

  @Test public void startShouldInvokeStartForeachTrackingTool() {
    tracker.start();

    verify(tools[0]).start();
  }

  @Test public void stopShouldInvokeStopForeachTrackingTool() {
    tracker.stop();

    verify(tools[0]).stop();
  }

  @Test public void doNotTrackEventWhenNotFiltered() {
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.FABRIC.getValue());

    HashSet<Integer> filter = new HashSet<>();
    filter.add(TrackerType.MIXPANEL.getValue());

    tracker.event("title", null, filter);

    verify(trackingAdapter, times(0)).trackEvent(anyString(), anyMap());
  }

  @Test public void onlyFilteredTrackersShouldCallTrackEvent() {
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.MIXPANEL.getValue());

    TrackingAdapter trackingAdapter2 = mock(TrackingAdapter.class);
    when(trackingAdapter2.getTrackerType()).thenReturn(TrackerType.FABRIC.getValue());

    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    Tracker tracker = spy(Tracker.init(tools));

    HashSet<Integer> filter = new HashSet<>();
    filter.add(TrackerType.MIXPANEL.getValue());

    tracker.event("title", null, filter);

    verify(trackingAdapter).trackEvent(anyString(), anyMap());
    verify(trackingAdapter2, times(0)).trackEvent(anyString(), anyMap());
  }

  @Test public void allTrackersShouldCallTrackEventWhenThereIsNoFilter() {
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.MIXPANEL.getValue());

    TrackingAdapter trackingAdapter2 = mock(TrackingAdapter.class);
    when(trackingAdapter2.getTrackerType()).thenReturn(TrackerType.MIXPANEL.getValue());

    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    Tracker tracker = spy(Tracker.init(tools));

    tracker.event("title", null, Collections.<Integer>emptySet());

    verify(trackingAdapter).trackEvent(anyString(), anyMap());
    verify(trackingAdapter2).trackEvent(anyString(), anyMap());
  }

  @Test public void trackEventShouldInvokeEventQueue() {
    TrackingAdapter trackingAdapter = new TrackingAdapter() {
      @Override public void trackEvent(String title, Map<String, Object> values) {

      }

      @Override public void start() {

      }

      @Override public void stop() {

      }

      @Override public int getTrackerType() {
        return 0;
      }

      @Override public String toString() {
        return "Tracker";
      }
    };
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    final Tracker tracker = spy(Tracker.init(tools));

    class Subscriber implements TrackEventSubscriber {
      EventItem item;

      @Override public void onEventAdded(EventItem item) {
        this.item = item;
      }
    }

    Subscriber subscriber = spy(new Subscriber());

    EventQueue.subscribe(subscriber);

    tracker.event("title", null);

    verify(subscriber).onEventAdded(any(EventItem.class));

    assertThat(subscriber.item.trackerName).isEqualTo("Tracker");
  }
}