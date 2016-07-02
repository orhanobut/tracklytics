package com.orhanobut.tracklytics.trackers;

import com.orhanobut.tracklytics.TrackEvent;

import java.util.Map;

public interface TrackingAdapter {

  void trackEvent(TrackEvent event, Map<String, Object> attributes, Map<String, Object> superAttributes);

  void start();

  void stop();

  int id();
}
