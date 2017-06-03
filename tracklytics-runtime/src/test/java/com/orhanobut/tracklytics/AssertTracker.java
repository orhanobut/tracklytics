package com.orhanobut.tracklytics;

import static com.google.common.truth.Truth.assertThat;

class AssertTracker {

  private Event event;

  AssertTracker(Event event) {
    this.event = event;
  }

  AssertTracker event(String name) {
    assertThat(event.name).isEqualTo(name);
    return this;
  }

  AssertTracker filters(int... tags) {
    for (int tag : tags) {
      assertThat(event.filters).asList().contains(tag);
    }
    return this;
  }

  AssertTracker noFilters() {
    assertThat(event.filters).isEmpty();
    return this;
  }

  AssertTracker tags(String... tags) {
    for (String tag : tags) {
      assertThat(event.tags).asList().contains(tag);
    }
    return this;
  }

  AssertTracker noTags() {
    assertThat(event.tags).isEmpty();
    return this;
  }

  AssertTracker attribute(String key, Object value) {
    assertThat(event.attributes).containsEntry(key, value);
    return this;
  }

  AssertTracker noAttributes() {
    assertThat(event.attributes).isEmpty();
    return this;
  }

  AssertTracker superAttribute(String key, Object value) {
    assertThat(event.superAttributes).containsEntry(key, value);
    return this;
  }

  void noSuperAttributes() {
    assertThat(event.superAttributes).isEmpty();
  }

}
