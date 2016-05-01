package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Tracker {

  Tracker init(TrackingAdapter... tools);

  void event(String title, Map<String, Object> values, Map<String, Object> superAttributes);

  void event(String title, Map<String, Object> attributes, Map<String, Object> superAttributes, Set<Integer> filter);

  Tracker enabled(boolean enabled);

  boolean isEnabled();

  void start();

  void stop();

  class Default implements Tracker {
    private TrackingAdapter[] tools;
    private boolean enabled = true;

    @Override public Tracker init(TrackingAdapter... tools) {
      this.tools = tools;
      return this;
    }

    @Override public void event(String title, Map<String, Object> values, Map<String, Object> superAttributes) {
      event(title, values, superAttributes, Collections.<Integer>emptySet());
    }

    @Override public void event(String title, Map<String, Object> attributes, Map<String, Object> superAttributes,
                                Set<Integer> filter) {
      if (!enabled) {
        return;
      }
      for (TrackingAdapter tool : tools) {
        if (filter.isEmpty() || filter.contains(tool.id())) {
          tool.trackEvent(title, attributes, superAttributes);
          EventQueue.add(tool.id(), tool.toString(), title, attributes);
        }
      }
    }

    @Override public Tracker enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    @Override public boolean isEnabled() {
      return enabled;
    }

    @Override public void start() {
      for (TrackingAdapter tool : tools) {
        tool.start();
      }
    }

    @Override public void stop() {
      for (TrackingAdapter tool : tools) {
        tool.stop();
      }
    }
  }

}