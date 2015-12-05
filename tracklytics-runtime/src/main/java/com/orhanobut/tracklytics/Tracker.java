package com.orhanobut.tracklytics;

import android.support.annotation.NonNull;
import android.util.Log;

import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Tracker {

  private static final String TAG = "Tracker";

  private final TrackingAdapter[] tools;

  private boolean enabled = true;

  public Tracker(TrackingAdapter[] tools) {
    this.tools = tools;
  }

  public static Tracker init(@NonNull TrackingAdapter... tools) {
    return new Tracker(tools);
  }

  public void event(String title, Map<String, Object> values) {
    event(title, values, Collections.<Integer>emptySet());
  }

  public void event(String title, Map<String, Object> values, @NonNull Set<Integer> filter) {
    if (!enabled) {
      return;
    }
    for (TrackingAdapter tool : tools) {
      if (filter.isEmpty() || filter.contains(tool.getTrackerType())) {
        tool.trackEvent(title, values);
        Log.d(TAG, tool.toString() + "---> EventName:" + title + ",  values:" + values.toString());
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