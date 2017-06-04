package com.orhanobut.tracklytics;

/**
 * Tracklytics aggregates all tracking event information and compile them.
 * Once the event is ready, Tracklytics emits the event to the subscribers.
 * <p>
 * This is a good place to implement your analytics tools such as Mixpanel, Firebase etc
 */
@SuppressWarnings("WeakerAccess")
public interface EventSubscriber {

  void onEventTracked(Event event);
}
