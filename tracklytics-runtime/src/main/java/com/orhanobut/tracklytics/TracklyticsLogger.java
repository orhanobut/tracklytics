package com.orhanobut.tracklytics;

/**
 * Subscribe to the pre-formatted log message.
 * Once there is a subscriber, log message will be sent through this interface
 */
public interface TracklyticsLogger {

  void log(String message);

}
