package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventItem;

public interface TrackEventSubscriber {
  void onEventAdded(EventItem item);
}
