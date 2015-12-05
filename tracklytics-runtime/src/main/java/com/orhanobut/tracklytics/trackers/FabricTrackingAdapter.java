package com.orhanobut.tracklytics.trackers;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class FabricTrackingAdapter implements TrackingAdapter {

  public FabricTrackingAdapter(Context context) {
    Fabric.with(context, new Crashlytics());
  }

  @Override public void trackEvent(String title, Map<String, Object> values) {
    CustomEvent customEvent = new CustomEvent(title);
    for (Map.Entry<String, Object> map : values.entrySet()) {
      customEvent.putCustomAttribute(map.getKey(), String.valueOf(map.getValue()));
    }
    Answers.getInstance().logCustom(customEvent);
  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public int getTrackerType() {
    return TrackerType.FABRIC.getValue();
  }

  @Override public String toString() {
    return "Fabric";
  }
}