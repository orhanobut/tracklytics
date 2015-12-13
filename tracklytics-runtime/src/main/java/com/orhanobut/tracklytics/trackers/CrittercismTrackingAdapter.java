package com.orhanobut.tracklytics.trackers;

import android.content.Context;

import com.crittercism.app.Crittercism;

import java.util.Map;

public class CrittercismTrackingAdapter implements TrackingAdapter {

  public CrittercismTrackingAdapter(Context context, String appId) {
    onCreate(context, appId);
  }

  public void onCreate(Context context, String appId) {
    Crittercism.initialize(context, appId);
  }

  @Override public void trackEvent(String title, Map<String, Object> values) {

  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public int getTrackerType() {
    return TrackerType.CRITTERCISM.getValue();
  }

  @Override public String toString() {
    return "Crittercism";
  }
}
