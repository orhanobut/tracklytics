package com.orhanobut.tracklytics.trackers;

public enum TrackerType {
  FABRIC(1),
  MIXPANEL(2);

  private final int value;

  TrackerType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
