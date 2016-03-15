package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Tracker {

  private final TrackingAdapter[] tools;

  private boolean enabled = true;

  public Tracker(TrackingAdapter[] tools) {
    this.tools = tools;
  }

  public static Tracker init(TrackingAdapter... tools) {
    return new Tracker(tools);
  }

  public void event(String title, Map<String, Object> values) {
    event(title, values, Collections.<Integer>emptySet());
  }

  public void event(String title, Map<String, Object> values, Set<Integer> filter) {
    if (!enabled) {
      return;
    }
    for (TrackingAdapter tool : tools) {
      if (filter.isEmpty() || filter.contains(tool.getTrackerType())) {
        tool.trackEvent(title, values);
        EventQueue.add(tool.getTrackerType(), tool.toString(), title, values);
      }
    }
  }

  public Tracker enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void start() {
    for (TrackingAdapter tool : tools) {
      tool.start();
    }
  }

  public void stop() {
    for (TrackingAdapter tool : tools) {
      tool.stop();
    }
  }

}