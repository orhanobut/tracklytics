package com.orhanobut.tracklytics;

import java.util.Map;

/**
 * Invoked when {@link TrackableAttribute} is set for a method parameter.
 * Given attributes will be used for the caller event
 */
@SuppressWarnings("WeakerAccess")
public interface Trackable {

  Map<String, Object> getTrackableAttributes();
}
