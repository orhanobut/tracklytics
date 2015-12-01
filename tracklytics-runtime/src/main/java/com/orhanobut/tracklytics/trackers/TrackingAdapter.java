package com.orhanobut.tracklytics.trackers;

import java.util.Map;

public interface TrackingAdapter {

  void trackEvent(String title, Map<String, Object> values);

  void start();

  void stop();

  int getTrackerType();
}
