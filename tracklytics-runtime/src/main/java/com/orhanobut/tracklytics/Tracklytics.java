package com.orhanobut.tracklytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Annotation based tracking event handler.
 */
public class Tracklytics implements AspectListener {

  private final Map<String, Object> superAttributes = new HashMap<>();
  private final EventSubscriber eventSubscriber;

  private EventLogListener logger;

  private Tracklytics(EventSubscriber eventSubscriber) {
    this.eventSubscriber = eventSubscriber;
  }

  public static Tracklytics init(EventSubscriber eventSubscriber) {
    Tracklytics tracklytics = new Tracklytics(eventSubscriber);
    TracklyticsAspect.subscribe(tracklytics);
    return tracklytics;
  }

  public void trackEvent(String eventName) {
    trackEvent(new Event(eventName, null, null, null, superAttributes));
  }

  public void trackEvent(String eventName, Map<String, Object> attributes) {
    trackEvent(new Event(eventName, null, null, attributes, superAttributes));
  }

  // TODO: For now keep it private
  private void trackEvent(Event event) {
    eventSubscriber.onEventTracked(event);
    log(event);
  }

  private void log(Event event) {
    if (logger == null) return;

    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder builder = new StringBuilder()
        .append(event.name)
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

  @Override public void onAspectEventTriggered(TrackEvent trackEvent, Map<String, Object> attributes) {
    trackEvent(new Event(trackEvent, attributes, superAttributes));
  }

  @Override public void onAspectSuperAttributeAdded(String key, Object value) {
    addSuperAttribute(key, value);
  }

  @Override public void onAspectSuperAttributeRemoved(String key) {
    removeSuperAttribute(key);
  }
}