package com.orhanobut.tracklytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Annotation based tracking event handler.
 */
public class Tracklytics {

  final Map<String, Object> superAttributes = new HashMap<>();

  private final EventSubscriber eventSubscriber;

  private EventLogListener logger;

  private Tracklytics(EventSubscriber eventSubscriber) {
    this.eventSubscriber = eventSubscriber;
  }

  public static Tracklytics init(EventSubscriber subscriber) {
    Tracklytics tracklytics = new Tracklytics(subscriber);
    TracklyticsAspect.init(tracklytics);
    return tracklytics;
  }

  void event(TrackEvent trackEvent, Map<String, Object> attributes) {
    eventSubscriber.onEvent(new Event(trackEvent, attributes, superAttributes));
  }

  public void trackEvent(String eventName, Map<String, Object> attributes) {
    eventSubscriber.onEvent(new Event(eventName, null, null, attributes, superAttributes));
  }

  void log(long start, long stopMethod, long stopTracking, TrackEvent event, Map<String, Object> attrs) {
    if (logger != null) {
      long method = TimeUnit.NANOSECONDS.toMillis(stopMethod - start);
      long total = TimeUnit.NANOSECONDS.toMillis(stopTracking - start);

      @SuppressWarnings("StringBufferReplaceableByString")
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
          .append(superAttributes.toString())
          .append(", filters: ")
          .append(Arrays.toString(event.filters()));
      logger.log(builder.toString());
    }
  }

  public void setEventLogListener(EventLogListener logger) {
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