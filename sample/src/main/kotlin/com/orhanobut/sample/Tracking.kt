package com.orhanobut.sample

import android.util.Log
import com.orhanobut.tracklytics.FixedAttribute
import com.orhanobut.tracklytics.TrackEvent

@FixedAttribute(key="screen_name", value = "Tracking")
open class Tracking {

  @TrackEvent("Event Kotlin")
  open fun trackScreenDisplayed() {
    Log.d("Tracking", "trackScreenDisplayed")
  }
}