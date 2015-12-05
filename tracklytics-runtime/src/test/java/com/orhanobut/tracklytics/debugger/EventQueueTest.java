package com.orhanobut.tracklytics.debugger;

import com.orhanobut.tracklytics.TrackEventSubscriber;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class EventQueueTest {

  List<TrackEventSubscriber> subscribers = spy(new ArrayList<TrackEventSubscriber>());

  @Before public void setup() {
    EventQueue.TRACK_EVENT_SUBSCRIBERS = subscribers;
  }

  @Test public void add() throws Exception {
    subscribers.add(spy(new TrackEventSubscriber() {
      @Override public void onEventAdded(EventItem item) {

      }
    }));

    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    verify(subscribers.get(0)).onEventAdded(any(EventItem.class));
  }

  @Test public void subscribe() throws Exception {
    TrackEventSubscriber subscriber = spy(new TrackEventSubscriber() {
      @Override public void onEventAdded(EventItem item) {

      }
    });

    EventQueue.subscribe(subscriber);

    verify(subscribers).add(subscriber);
  }

  @Test public void cacheItemsWhenNoSubscribers() {
    EventQueue.TRACK_EVENT_SUBSCRIBERS.clear();

    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    assertThat(EventQueue.getUndispatched()).hasSize(1);
  }

  @Test public void getUndispatchedShouldClearList() {
    EventQueue.TRACK_EVENT_SUBSCRIBERS.clear();
    EventQueue.add(1, "Tracker", "title", Collections.<String, Object>emptyMap());

    List<EventItem> list = EventQueue.getUndispatched();

    assertThat(EventQueue.undispatched).hasSize(0);
    assertThat(list).hasSize(1);
  }
}