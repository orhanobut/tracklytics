package com.orhanobut.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.trackers.SimpleTrackingAdapter;

import java.util.Map;

public class MainActivity extends Activity {


  @Override @TrackEvent("Event Java")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Tracker.init(new SimpleTrackingAdapter() {
      @Override public void trackEvent(TrackEvent event, Map<String, Object> attributes,
                                       Map<String, Object> superAttributes) {
        super.trackEvent(event, attributes, superAttributes);

        Log.d("TrackingSample", event.value());
      }
    });

    new Tracking().trackScreenDisplayed();

    new Foo().trackFoo();
  }
}
