package com.orhanobut.tracklytics.trackers;

import java.util.Map;

public class SnowPlowTrackingAdapter implements TrackingAdapter {

  @Override public void trackEvent(String title, Map<String, Object> values) {

  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public int getTrackerType() {
    return TrackerType.SNOWPLOW.getValue();
  }
}