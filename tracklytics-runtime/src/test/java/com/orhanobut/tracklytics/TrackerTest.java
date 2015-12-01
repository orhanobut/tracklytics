package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackerType;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackerTest {

  @Test public void eventShouldInvokeToolsTrackEventWhenDisabled() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = Tracker.init(tools).enabled(false);
    tracker.event("title", null);

    verify(trackingAdapter, never()).trackEvent("title", null);
  }

  @Test public void isEnabledShouldReturnTrueAsDefault() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = Tracker.init(tools);
    assertThat(tracker.isEnabled()).isTrue();
  }

  @Test public void isEnabledShouldReturnFalse() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = Tracker.init(tools).enabled(false);
    assertThat(tracker.isEnabled()).isFalse();
  }

  @Test public void startShouldInvokeStartForeachTrackingTool() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = spy(Tracker.init(tools));

    tracker.start();

    verify(tools[0]).start();
  }

  @Test public void stopShouldInvokeStopForeachTrackingTool() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = spy(Tracker.init(tools));

    tracker.stop();

    verify(tools[0]).stop();
  }

  @Test public void trackEventShouldNotBeCalledWhenNotFiltered() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.ADJUST.getValue());

    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter};
    Tracker tracker = spy(Tracker.init(tools));

    HashSet<Integer> filter = new HashSet<>();
    filter.add(TrackerType.CRITTERCISM.getValue());

    tracker.event("title", null, filter);

    verify(trackingAdapter, times(0)).trackEvent(anyString(), anyMap());
  }

  @Test public void onlyFilteredTrackersShouldCallTrackEvent() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.ADJUST.getValue());

    TrackingAdapter trackingAdapter2 = mock(TrackingAdapter.class);
    when(trackingAdapter2.getTrackerType()).thenReturn(TrackerType.CRITTERCISM.getValue());

    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    Tracker tracker = spy(Tracker.init(tools));

    HashSet<Integer> filter = new HashSet<>();
    filter.add(TrackerType.ADJUST.getValue());

    tracker.event("title", null, filter);

    verify(trackingAdapter).trackEvent(anyString(), anyMap());
    verify(trackingAdapter2, times(0)).trackEvent(anyString(), anyMap());
  }

  @Test public void allTrackersShouldCallTrackEventWhenThereIsNoFilter() {
    TrackingAdapter trackingAdapter = mock(TrackingAdapter.class);
    when(trackingAdapter.getTrackerType()).thenReturn(TrackerType.ADJUST.getValue());

    TrackingAdapter trackingAdapter2 = mock(TrackingAdapter.class);
    when(trackingAdapter2.getTrackerType()).thenReturn(TrackerType.CRITTERCISM.getValue());

    TrackingAdapter[] tools = new TrackingAdapter[]{trackingAdapter, trackingAdapter2};
    Tracker tracker = spy(Tracker.init(tools));

    tracker.event("title", null, Collections.<Integer>emptySet());

    verify(trackingAdapter).trackEvent(anyString(), anyMap());
    verify(trackingAdapter2).trackEvent(anyString(), anyMap());
  }
}