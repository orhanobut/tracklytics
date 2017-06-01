package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Tracklytics {

  final Map<String, Object> superAttributes = new HashMap<>();

  private final TrackingAdapter[] adapters;

  private boolean enabled = true;
  private TracklyticsLogger logger;

  private Tracklytics(TrackingAdapter[] adapters) {
    this.adapters = adapters;
  }

  public static Tracklytics init(TrackingAdapter... adapters) {
    Tracklytics tracklytics = new Tracklytics(adapters);
    TracklyticsAspect.init(tracklytics);
    return tracklytics;
  }

  void event(TrackEvent trackEvent, Map<String, Object> attributes, Map<String, Object> superAttributes) {
    if (!enabled) return;

    for (TrackingAdapter tool : adapters) {
      tool.trackEvent(new Event(trackEvent, attributes), superAttributes);
    }
  }

  public void trackEvent(Event event) {
    if (!enabled) return;

    for (TrackingAdapter tool : adapters) {
      tool.trackEvent(event, superAttributes);
    }
  }

  Tracklytics enabled(boolean enabled) {
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
          .append(", filters: ")
          .append(Arrays.toString(event.filters()));
      logger.log(builder.toString());
    }
  }

  public void setLogger(TracklyticsLogger logger) {
    this.logger = logger;
  }

  /**
   * Allows you to add super attribute without requiring to use annotation
   */
  public void addSuperAttribute(String key, Object value) {
    this.superAttributes.put(key, value);
  }

  /**
   * Allows you to remove super attribute without requiring to use annotation
   */
  public void removeSuperAttribute(String key) {
    this.superAttributes.remove(key);
  }

}