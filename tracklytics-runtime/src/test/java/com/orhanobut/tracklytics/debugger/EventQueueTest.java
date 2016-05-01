package com.orhanobut.tracklytics.debugger;

import com.orhanobut.tracklytics.TrackEventSubscriber;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventQueueTest {

  List<TrackEventSubscriber> subscribers = EventQueue.TRACK_EVENT_SUBSCRIBERS;

  @Test public void add() throws Exception {
    TrackEventSubscriber subscriber = mock(TrackEventSubscriber.class);
    subscribers.add(subscriber);

    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    verify(subscriber).onEventAdded(any(EventItem.class));
  }

  @Test public void subscribe() throws Exception {
    TrackEventSubscriber subscriber = mock(TrackEventSubscriber.class);
    EventQueue.subscribe(subscriber);

    assertThat(subscribers).contains(subscriber);
  }

  @Test public void cacheItemsWhenNoSubscribers() {
    EventQueue.TRACK_EVENT_SUBSCRIBERS.clear();

    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    assertThat(EventQueue.pollUndispatched()).hasSize(1);
  }

  @Test public void pollUndispatchedShouldClearList() {
    EventQueue.TRACK_EVENT_SUBSCRIBERS.clear();
    EventQueue.clearAll();
    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    List<EventItem> list = EventQueue.pollUndispatched();

    assertThat(EventQueue.UNDISPATCHED).isEmpty();
    assertThat(list).hasSize(1);
  }
}