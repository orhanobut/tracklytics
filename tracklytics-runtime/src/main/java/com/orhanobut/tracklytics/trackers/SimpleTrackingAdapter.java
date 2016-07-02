package com.orhanobut.tracklytics.trackers;

import com.orhanobut.tracklytics.TrackEvent;

import java.util.Map;

public class SimpleTrackingAdapter implements TrackingAdapter {

  @Override public void trackEvent(TrackEvent event, Map<String, Object> attributes,
                                   Map<String, Object> superAttributes) {
  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public int id() {
    return 0;
  }
}
