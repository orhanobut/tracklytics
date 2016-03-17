package com.orhanobut.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orhanobut.tracklytics.Attribute;
import com.orhanobut.tracklytics.Track;
import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.TrackerAction;
import com.orhanobut.tracklytics.Tracklytics;
import com.orhanobut.tracklytics.TracklyticsDebugger;
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
        event1("test");
      }
    });

    findViewById(R.id.no_return_button).setOnClickListener(new View.OnClickListener() {
      @Track(eventName = "OnClick", attributeKey = "key", attributeValue = "value")
      @Override public void onClick(View v) {
        eventNoReturn();
      }
    });

    init();

    TracklyticsDebugger.inject(this);
  }

  @Tracklytics(TrackerAction.INIT) Tracker init() {
    return Tracker.init(
        new MixPanelTrackingAdapter(this, "API_KEY")
    );
  }

  @TrackEvent("event1") @Attribute("test") String event1(@Attribute("key") String a) {
    return "test";
  }

  @Track(eventName = "test", attributeKey = "key", attributeValue = "value") void eventNoReturn() {
  }

}