package com.orhanobut.tracklytics.debugger;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class EventItem {
  public final int trackerType;
  public final String trackerName;
  public final String eventName;
  public final Map<String, Object> eventValues;
  public final Date eventDate;

  public EventItem(int trackerType, String trackerName, String eventName, Map<String, Object> values) {
    this.eventDate = Calendar.getInstance().getTime();
    this.trackerType = trackerType;
    this.trackerName = trackerName;
    this.eventName = eventName;
    this.eventValues = values;
  }
}
