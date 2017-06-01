package com.orhanobut.tracklytics;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class AssertTracker {

  @Captor ArgumentCaptor<Map<String, Object>> captorAttributes;
  @Captor ArgumentCaptor<Map<String, Object>> captorSuperAttributes;
  @Captor ArgumentCaptor<TrackEvent> captorTrackEvent;
  @Captor ArgumentCaptor<Set<Integer>> captorFilters;

  private final TrackEvent trackEvent;
  private final Map<String, Object> attributes;
  private final Map<String, Object> superAttributes;

  private AssertTracker(Tracklytics tracklytics) {
    initMocks(this);

    verify(tracklytics, atLeastOnce()).event(
        captorTrackEvent.capture(),
        captorAttributes.capture(),
        captorSuperAttributes.capture()
    );

    trackEvent = captorTrackEvent.getValue();
    attributes = captorAttributes.getValue();
    superAttributes = captorSuperAttributes.getValue();
  }

  static AssertTracker assertTrack(Tracklytics tracklytics) {
    return new AssertTracker(tracklytics);
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

  AssertTracker superAttribute(String key, Object value) {
    assertThat(superAttributes).containsEntry(key, value);
    return this;
  }

  void noSuperAttributes() {
    assertThat(superAttributes).isEmpty();
  }

}
