package com.orhanobut.tracklytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

  void trackEvent(TrackEvent trackEvent, Map<String, Object> attributes) {
    trackEvent(new Event(trackEvent, attributes, superAttributes));
  }

  public void trackEvent(String eventName, Map<String, Object> attributes) {
    trackEvent(new Event(eventName, null, null, attributes, superAttributes));
  }

  // TODO: For now keep it private
  private void trackEvent(Event event) {
    eventSubscriber.onEvent(event);
    log(event);
  }

  private void log(Event event) {
    if (logger == null) return;

    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder builder = new StringBuilder()
        .append(event.eventName)
        .append("-> ")
        .append(event.attributes.toString())
        .append(", super attrs: ")
        .append(superAttributes.toString())
        .append(", filters: ")
        .append(Arrays.toString(event.filters));
    logger.log(builder.toString());
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