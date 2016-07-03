package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Tracker {

  final Map<String, Object> superAttributes = new HashMap<>();

  private final TrackingAdapter[] adapters;

  private boolean enabled = true;
  private TracklyticsLogger logger;

  private Tracker(TrackingAdapter[] adapters) {
    this.adapters = adapters;
  }

  public static Tracker init(TrackingAdapter... adapters) {
    Tracker tracker = new Tracker(adapters);
    TrackerAspect.init(tracker);
    return tracker;
  }

  void event(TrackEvent trackEvent, Map<String, Object> attributes, Map<String, Object> superAttributes) {
    if (!enabled) return;

    for (TrackingAdapter tool : adapters) {
      tool.trackEvent(trackEvent, attributes, superAttributes);
      EventQueue.add(tool.id(), tool.toString(), trackEvent.value(), attributes);
    }
  }

  Tracker enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  boolean isEnabled() {
    return enabled;
  }

  void start() {
    for (TrackingAdapter tool : adapters) {
      tool.start();
    }
  }

  void stop() {
    for (TrackingAdapter tool : adapters) {
      tool.stop();
    }
  }

  void log(long start, long stopMethod, long stopTracking, TrackEvent event, Map<String, Object> attrs,
           Map<String, Object> superAttrs) {
    if (logger != null) {
      long method = TimeUnit.NANOSECONDS.toMillis(stopMethod - start);
      long total = TimeUnit.NANOSECONDS.toMillis(stopTracking - start);
      StringBuilder builder = new StringBuilder()
          .append("[")
          .append(method)  // Method execution time
          .append("+")
          .append(total - method)  // Tracking execution time
          .append("=")
          .append(total)  // Total execution time
          .append("ms] ")
          .append(event.value())
          .append("-> ")
          .append(attrs.toString())
          .append(", super attrs: ")
          .append(superAttrs.toString())
          .append(", tags: ")
          .append(Arrays.toString(event.tags()));
      logger.log(builder.toString());
    }
  }

  public void setLogger(TracklyticsLogger logger) {
    this.logger = logger;
  }
}