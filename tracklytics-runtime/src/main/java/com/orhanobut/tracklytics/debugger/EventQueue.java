package com.orhanobut.tracklytics.debugger;

import com.orhanobut.tracklytics.TrackEventSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventQueue {

  static final List<TrackEventSubscriber> TRACK_EVENT_SUBSCRIBERS = new ArrayList<>();
  static final List<EventItem> UNDISPATCHED = new ArrayList<>();

  private EventQueue() {
    // no instance
  }

  public static void add(int trackerType, String trackerName, String title, Map<String, Object> values) {
    EventItem item = new EventItem(trackerType, trackerName, title, values);
    if (TRACK_EVENT_SUBSCRIBERS.isEmpty()) {
      UNDISPATCHED.add(item);
      return;
    }
    for (TrackEventSubscriber trackEventSubscriber : TRACK_EVENT_SUBSCRIBERS) {
      trackEventSubscriber.onEventAdded(item);
    }
  }

  public static void subscribe(TrackEventSubscriber trackEventSubscriber) {
    TRACK_EVENT_SUBSCRIBERS.add(trackEventSubscriber);
  }

  public static List<EventItem> pollUndispatched() {
    List<EventItem> list = new ArrayList<>(UNDISPATCHED);
    UNDISPATCHED.clear();
    return list;
  }

  public static void clearAll() {
    UNDISPATCHED.clear();
  }
}
