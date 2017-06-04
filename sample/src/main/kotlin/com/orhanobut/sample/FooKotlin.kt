package com.orhanobut.sample

import com.orhanobut.tracklytics.FixedAttribute
import com.orhanobut.tracklytics.TrackEvent

@FixedAttribute(key="screen_name", value = "FooKotlin")
open class FooKotlin {

  @TrackEvent("event_kotlin")
  open fun trackFoo() {
  }
}