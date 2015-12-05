package com.orhanobut.tracklytics.debugger;

import android.app.Activity;

import com.orhanobut.tracklytics.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class UiHandlerTest {

  private Activity activity;

  @Before public void setup() {
    activity = Robolectric.setupActivity(Activity.class);
  }

  @Test public void injectShouldSubscribeToEventQueue() throws Exception {
    EventQueue.TRACK_EVENT_SUBSCRIBERS.clear();

    new UiHandler(activity).inject();

    assertThat(EventQueue.TRACK_EVENT_SUBSCRIBERS).hasSize(1);
  }
}