package com.orhanobut.sample.trackers;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Map;

public class MixPanelTrackingAdapter implements TrackingAdapter {

  private MixpanelAPI mixpanelAPI;

  public MixPanelTrackingAdapter(Context context, String apiKey) {
    mixpanelAPI = MixpanelAPI.getInstance(context, apiKey);
  }

  @Override public void trackEvent(String title, Map<String, Object> values, Map<String, Object> superAttributes) {
    mixpanelAPI.trackMap(title, values);
  }

  @Override public void start() {
  }

  @Override public void stop() {
    mixpanelAPI.flush();
  }

  @Override public int id() {
    return 100;
  }

  @Override public String toString() {
    return "MixPanel";
  }

}