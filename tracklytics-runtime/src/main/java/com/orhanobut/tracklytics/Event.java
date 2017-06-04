package com.orhanobut.tracklytics;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the container for the triggered tracking event.
 * All related information is kept in here.
 */
@SuppressWarnings("WeakerAccess")
public class Event {

  public final String name;
  public final int[] filters;
  public final String[] tags;
  public final Map<String, Object> attributes;
  public final Map<String, Object> superAttributes;

  public Event(String eventName, int[] filters, String[] tags, Map<String, Object> attributes,
               Map<String, Object> superAttributes) {
    this.name = eventName;
    this.filters = filters;
    this.tags = tags;
    this.attributes = attributes;
    this.superAttributes = superAttributes;
  }

  public Event(TrackEvent trackEvent, Map<String, Object> attributes, Map<String, Object> superAttributes) {
    this.name = trackEvent.value();
    this.filters = trackEvent.filters();
    this.tags = trackEvent.tags();
    this.attributes = attributes;
    this.superAttributes = superAttributes;
  }

  public Map<String, Object> getAllAttributes() {
    Map<String, Object> allAttributes = new HashMap<>();
    allAttributes.putAll(attributes);
    allAttributes.putAll(superAttributes);
    return allAttributes;
  }
}
