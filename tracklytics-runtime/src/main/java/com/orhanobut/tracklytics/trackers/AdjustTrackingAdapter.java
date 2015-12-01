package com.orhanobut.tracklytics.trackers;

import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;

import java.util.Map;

public class AdjustTrackingAdapter implements TrackingAdapter {

  public enum Environment {
    STAGING(AdjustConfig.ENVIRONMENT_SANDBOX),
    LIVE(AdjustConfig.ENVIRONMENT_PRODUCTION);

    final String environment;

    Environment(String environment) {
      this.environment = environment;
    }
  }

  public AdjustTrackingAdapter(Context context, String appToken, Environment environment) {
    AdjustConfig config = new AdjustConfig(context, appToken, environment.environment);
    Adjust.onCreate(config);
  }

  @Override public void trackEvent(String title, Map<String, Object> values) {
    AdjustEvent event = new AdjustEvent(title);

    if (values != null) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        event.addCallbackParameter(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }

    Adjust.trackEvent(event);
  }

  @Override public void start() {
    Adjust.onResume();
  }

  @Override public void stop() {
    Adjust.onPause();
  }

  @Override public int getTrackerType() {
    return TrackerType.ADJUST.getValue();
  }
}