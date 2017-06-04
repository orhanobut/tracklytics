package com.orhanobut.tracklytics;

/**
 * Subscribe to the pre-formatted log message stream.
 * Once there is a subscriber, log message will be sent through this interface
 */
@SuppressWarnings("WeakerAccess")
public interface EventLogListener {

  void log(String message);

}
