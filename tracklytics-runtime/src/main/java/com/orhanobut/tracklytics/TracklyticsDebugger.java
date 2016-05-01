package com.orhanobut.tracklytics;

import android.app.Activity;

import com.orhanobut.tracklytics.debugger.UiHandler;

public final class TracklyticsDebugger {

  private TracklyticsDebugger() {
    // no instance
  }

  public static void inject(Activity activity) {
    new UiHandler(activity).inject();
  }
}
