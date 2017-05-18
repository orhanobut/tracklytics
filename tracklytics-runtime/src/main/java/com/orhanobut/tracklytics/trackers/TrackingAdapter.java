package com.orhanobut.tracklytics.trackers;

import com.orhanobut.tracklytics.Event;

import java.util.Map;

public interface TrackingAdapter {

  void trackEvent(Event event, Map<String, Object> superAttributes);

  void start();

  void stop();

  int id();
}
