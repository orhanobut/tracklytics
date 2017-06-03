package com.orhanobut.tracklytics;

import java.util.Map;

interface AspectListener {

  void onAspectEventTriggered(TrackEvent trackEvent, Map<String, Object> attributes);

  void onAspectSuperAttributeAdded(String key, Object value);

  void onAspectSuperAttributeRemoved(String key);
}