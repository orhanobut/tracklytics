package com.orhanobut.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.TrackValue;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.TrackerAction;
import com.orhanobut.tracklytics.Tracklytics;
import com.orhanobut.tracklytics.TracklyticsDebugger;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        event1("test");
      }
    });

    init();

    TracklyticsDebugger.inject(this);

    event1("test");
  }

  @Tracklytics(TrackerAction.INIT) Tracker init() {
    return Tracker.init(
        new TrackingAdapter() {
          @Override public void trackEvent(String title, Map<String, Object> values) {
            Log.d("tag", title);
          }

          @Override public void start() {

          }

          @Override public void stop() {

          }

          @Override public int getTrackerType() {
            return 10;
          }

          @Override public String toString() {
            return "Tracker";
          }
        },
        new TrackingAdapter() {
          @Override public void trackEvent(String title, Map<String, Object> values) {
            Log.d("tag", title);
          }

          @Override public void start() {

          }

          @Override public void stop() {

          }

          @Override public int getTrackerType() {
            return 10;
          }

          @Override public String toString() {
            return "Tracker2";
          }
        }

//        new MixPanelTrackingAdapter(this, "API_KEY"),
//        new GoogleAnalyticsTrackingAdapter(this, "CONTAINER_ID", R.raw.container),
//        new CrittercismTrackingAdapter(this, "APP_ID"),
//        new AdjustTrackingAdapter(this, "APPTOKEN", AdjustTrackingAdapter.Environment.LIVE),
//        new FabricTrackingAdapter(this)
    );
  }

  @TrackEvent("event1") @TrackValue("test") String event1(@TrackValue("key") String a) {
    return "test";
  }
}