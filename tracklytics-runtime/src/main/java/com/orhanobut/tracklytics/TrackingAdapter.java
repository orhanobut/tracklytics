package com.orhanobut.tracklytics;

import java.util.Map;

/**
 * TrackingAdapter is the output channel for Tracklytics.
 * By using this interface, you can send the tracked events to your servers
 */
@SuppressWarnings("WeakerAccess")
public interface TrackingAdapter {

  void trackEvent(Event event, Map<String, Object> superAttributes);
}
