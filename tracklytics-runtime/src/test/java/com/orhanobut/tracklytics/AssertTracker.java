package com.orhanobut.tracklytics;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

class AssertTracker {

  private final TrackEvent trackEvent;
  private final Map<String, Object> attributes;

  AssertTracker(TrackEvent trackEvent, Map<String, Object> attributes) {
    this.trackEvent = trackEvent;
    this.attributes = attributes;
  }

  AssertTracker event(String name) {
    assertThat(trackEvent.value()).isEqualTo(name);
    return this;
  }

  AssertTracker filters(int... tags) {
    for (int tag : tags) {
      assertThat(trackEvent.filters()).asList().contains(tag);
    }
    return this;
  }

  AssertTracker noFilters() {
    assertThat(trackEvent.filters()).isEmpty();
    return this;
  }

  AssertTracker tags(String... tags) {
    for (String tag : tags) {
      assertThat(trackEvent.tags()).asList().contains(tag);
    }
    return this;
  }

  AssertTracker noTags() {
    assertThat(trackEvent.tags()).isEmpty();
    return this;
  }

  AssertTracker attribute(String key, Object value) {
    assertThat(attributes).containsEntry(key, value);
    return this;
  }

  AssertTracker noAttributes() {
    assertThat(attributes).isEmpty();
    return this;
  }
}
