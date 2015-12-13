package com.orhanobut.tracklytics.trackers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoogleAnalyticsTrackingAdapter implements TrackingAdapter {

  private ContainerHolder containerHolder;
  private DataLayer dataLayer;

  public GoogleAnalyticsTrackingAdapter(Context context, String containerId, int containerResId) {
    onCreate(context, containerId, containerResId);
  }

  public void onCreate(Context context, String containerId, int containerResId) {
    TagManager tagManager = TagManager.getInstance(context);
    PendingResult<ContainerHolder> pending =
        tagManager.loadContainerPreferNonDefault(containerId, containerResId);

    pending.setResultCallback(new ResultCallback<ContainerHolder>() {
      @Override
      public void onResult(ContainerHolder containerHolder) {
        GoogleAnalyticsTrackingAdapter.this.containerHolder = containerHolder;
        if (!containerHolder.getStatus().isSuccess()) {
          Log.e("CuteAnimals", "failure loading container");
        }
      }
    }, 2, TimeUnit.SECONDS);

    dataLayer = tagManager.getDataLayer();
  }

  @Override public void trackEvent(String title, Map<String, Object> values) {
    dataLayer.pushEvent(title, values);
  }

  @Override public void start() {
  }

  @Override public void stop() {
  }

  @Override public int getTrackerType() {
    return TrackerType.GOOGLE_ANALYTICS.getValue();
  }

  @Override public String toString() {
    return "GoogleAnalytics";
  }

}
