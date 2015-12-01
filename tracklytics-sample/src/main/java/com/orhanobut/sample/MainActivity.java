package com.orhanobut.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.TrackValue;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.TrackerAction;
import com.orhanobut.tracklytics.Tracklytics;
import com.orhanobut.tracklytics.trackers.AdjustTrackingAdapter;
import com.orhanobut.tracklytics.trackers.CrittercismTrackingAdapter;
import com.orhanobut.tracklytics.trackers.FabricTrackingAdapter;
import com.orhanobut.tracklytics.trackers.GoogleAnalyticsTrackingAdapter;
import com.orhanobut.tracklytics.trackers.MixPanelTrackingAdapter;

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
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });

    init();
  }

  @Tracklytics(TrackerAction.INIT) Tracker init() {
    return Tracker.init(
        new MixPanelTrackingAdapter(this, "API_KEY"),
        new GoogleAnalyticsTrackingAdapter(this, "CONTAINER_ID", R.raw.container),
        new CrittercismTrackingAdapter(this, "APP_ID"),
        new AdjustTrackingAdapter(this, "APPTOKEN", AdjustTrackingAdapter.Environment.LIVE),
        new FabricTrackingAdapter(this)
    );
  }

  @TrackEvent("event1") @TrackValue("test") String event1(@TrackValue("key") String a) {
    return "test";
  }
}