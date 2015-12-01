package com.orhanobut.tracklytics.trackers;

public enum TrackerType {
  ADJUST(1),
  CRITTERCISM(2),
  FABRIC(3),
  GOOGLE_ANALYTICS(4),
  MIXPANEL(5),
  SNOWPLOW(6);

  private final int value;

  TrackerType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
