package com.orhanobut.tracklytics.trackers;

import com.orhanobut.tracklytics.Event;

import java.util.Map;

public class SimpleTrackingAdapter implements TrackingAdapter {

  @Override public void trackEvent(Event event, Map<String, Object> superAttributes) {

  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public int id() {
    return 0;
  }
}
