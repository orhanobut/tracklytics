package com.orhanobut.sample

import android.util.Log
import com.orhanobut.tracklytics.TrackEvent

open class Tracking {

  @TrackEvent("Event Kotlin")
  open fun trackScreenDisplayed() {
    Log.d("Tracking", "trackScreenDisplayed")
  }
}