package com.orhanobut.tracklytics;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class EventTest {

  @Test public void defaultValues() {
    Map<String, Object> attributes = new HashMap<>();
    Map<String, Object> superAttributes = new HashMap<>();

    int[] filters = {1, 3, 4};
    String[] tags = {"a", "b"};

    Event event = new Event("event_name", filters, tags, attributes, superAttributes);

    assertThat(event.eventName).isEqualTo("event_name");
    assertThat(event.filters).isEqualTo(filters);
    assertThat(event.tags).isEqualTo(tags);
    assertThat(event.attributes).isEqualTo(attributes);
    assertThat(event.superAttributes).isEqualTo(superAttributes);
  }
}