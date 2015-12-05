package com.orhanobut.tracklytics.debugger;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EventItemTest {
  @Test public void testConstructor() {
    Map<String, Object> map = new HashMap<>();
    map.put("key", "value");
    EventItem item = new EventItem(1, "Tracker", "eventName", map);

    assertThat(item.eventDate).isNotNull();
    assertThat(item.eventName).isEqualTo("eventName");
    assertThat(item.eventValues).isEqualTo(map);
    assertThat(item.trackerType).isEqualTo(1);
    assertThat(item.trackerName).isEqualTo("Tracker");
  }
}