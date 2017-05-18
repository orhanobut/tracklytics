package com.orhanobut.tracklytics;

import java.util.Map;

public class Event {

  public final String eventName;
  public final int[] filters;
  public final String[] tags;
  public final Map<String, Object> attributes;


  public Event(String eventName, int[] filters, String[] tags, Map<String, Object> attributes) {
    this.eventName = eventName;
    this.filters = filters;
    this.tags = tags;
    this.attributes = attributes;
  }

  public Event(TrackEvent trackEvent, Map<String, Object> attributes) {
    this.eventName = trackEvent.value();
    this.filters = trackEvent.filters();
    this.tags = trackEvent.tags();
    this.attributes = attributes;
  }
}
