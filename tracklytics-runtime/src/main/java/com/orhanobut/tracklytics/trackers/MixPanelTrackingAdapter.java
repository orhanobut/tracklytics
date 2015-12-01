package com.orhanobut.tracklytics.trackers;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Map;

public class MixPanelTrackingAdapter implements TrackingAdapter {

  private MixpanelAPI mixpanelAPI;

  public MixPanelTrackingAdapter(Context context, String apiKey) {
    mixpanelAPI = MixpanelAPI.getInstance(context, apiKey);
  }

  @Override public void trackEvent(String title, Map<String, Object> values) {
    mixpanelAPI.trackMap(title, values);
  }

  @Override public void start() {
  }

  @Override public void stop() {
    mixpanelAPI.flush();
  }

  @Override public int getTrackerType() {
    return TrackerType.MIXPANEL.getValue();
  }

}