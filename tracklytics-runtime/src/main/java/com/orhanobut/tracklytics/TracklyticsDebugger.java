package com.orhanobut.tracklytics;

import android.app.Activity;

import com.orhanobut.tracklytics.debugger.UiHandler;

public class TracklyticsDebugger {

  public static void inject(Activity activity) {
    new UiHandler(activity).inject();
  }
}
