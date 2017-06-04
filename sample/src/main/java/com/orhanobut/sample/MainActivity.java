package com.orhanobut.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.orhanobut.tracklytics.Attribute;
import com.orhanobut.tracklytics.Event;
import com.orhanobut.tracklytics.EventLogListener;
import com.orhanobut.tracklytics.EventSubscriber;
import com.orhanobut.tracklytics.FixedAttribute;
import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.Trackable;
import com.orhanobut.tracklytics.TrackableAttribute;
import com.orhanobut.tracklytics.Tracklytics;
import com.orhanobut.tracklytics.TransformAttribute;
import com.orhanobut.tracklytics.TransformAttributeMap;

import java.util.HashMap;
import java.util.Map;

@FixedAttribute(key = "screen_name", value = "Login")
public class MainActivity extends Activity implements Trackable {

  @TrackEvent("on_create")
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Tracklytics.init(new EventSubscriber() {
      @Override public void onEventTracked(Event event) {
        // Send your events to Mixpanel, Fabric etc
      }
    }).setEventLogListener(new EventLogListener() {
      @Override public void log(String message) {
        // Set your logger here. ie: Logger or Timber
        Log.d("Tracker", message);
      }
    });

    findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

      @TrackEvent("button_click")
      @FixedAttribute(key = "button_name", value = "Login")
      @Override public void onClick(View v) {

      }
    });
  }

  @TrackEvent("login")
  private void onLoggedIn(@TrackableAttribute User user, @Attribute("id") String id) {
  }

  @TrackEvent("transform")
  @TransformAttributeMap(
      keys = {1, 2},
      values = {"finished", "accepted"}
  )
  private void onItemSelected(@TransformAttribute("status") int position) {
  }

  @TrackEvent("another_event")
  @Attribute("user_id") // This attribute will use return value as attribute value.
  private String userId() {
    return "2342";
  }

  /**
   * For each event this attributes will be used, same as screen_name
   */
  @Override public Map<String, Object> getTrackableAttributes() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("user_id", "2234");
    return attributes;
  }

  private static class User implements Trackable {
    final String name;
    final String email;

    User(String name, String email) {
      this.name = name;
      this.email = email;
    }

    @Override public Map<String, Object> getTrackableAttributes() {
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("email", email);
      attributes.put("name", name);
      return attributes;
    }
  }
}
