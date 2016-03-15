package com.orhanobut.tracklytics.debugger;

import com.orhanobut.tracklytics.TrackEventSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventQueue {

  static List<TrackEventSubscriber> TRACK_EVENT_SUBSCRIBERS = new ArrayList<>();
  static List<EventItem> undispatched = new ArrayList<>();

  public static void add(int trackerType, String trackerName, String title, Map<String, Object> values) {
    EventItem item = new EventItem(trackerType, trackerName, title, values);
    if (TRACK_EVENT_SUBSCRIBERS.isEmpty()) {
      undispatched.add(item);
      return;
    }
    for (TrackEventSubscriber trackEventSubscriber : TRACK_EVENT_SUBSCRIBERS) {
      trackEventSubscriber.onEventAdded(item);
    }
  }

  public static void subscribe(TrackEventSubscriber trackEventSubscriber) {
    TRACK_EVENT_SUBSCRIBERS.add(trackEventSubscriber);
  }

  public static List<EventItem> getUndispatched() {
    List<EventItem> list = new ArrayList<>(undispatched);
    undispatched.clear();
    return list;
  }

  public static void clearAll() {
    undispatched.clear();
  }
}
