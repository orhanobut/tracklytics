package com.orhanobut.tracklytics;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AssertTracker {

  @Captor ArgumentCaptor<Map<String, Object>> captorAttributes;
  @Captor ArgumentCaptor<Map<String, Object>> captorSuperAttributes;
  @Captor ArgumentCaptor<TrackEvent> captorTrackEvent;
  @Captor ArgumentCaptor<Set<Integer>> captorFilters;

  private final TrackEvent trackEvent;
  private final Map<String, Object> attributes;
  private final Map<String, Object> superAttributes;

  private AssertTracker(Tracker tracker) {
    initMocks(this);

    verify(tracker, atLeastOnce()).event(
        captorTrackEvent.capture(),
        captorAttributes.capture(),
        captorSuperAttributes.capture()
    );

    trackEvent = captorTrackEvent.getValue();
    attributes = captorAttributes.getValue();
    superAttributes = captorSuperAttributes.getValue();
  }

  public static AssertTracker assertTrack(Tracker tracker) {
    return new AssertTracker(tracker);
  }

  public AssertTracker event(String name) {
    assertThat(trackEvent.value()).isEqualTo(name);
    return this;
  }

  public AssertTracker tags(int... tags) {
    for (int tag : tags) {
      assertThat(trackEvent.tags()).asList().contains(tag);
    }
    return this;
  }

  public AssertTracker noTags() {
    assertThat(trackEvent.tags()).isEmpty();
    return this;
  }

  public AssertTracker attribute(String key, Object value) {
    assertThat(attributes).containsEntry(key, value);
    return this;
  }

  public AssertTracker doesNotContainAttribute(String key, Object value) {
    assertThat(attributes).doesNotContainEntry(key, value);
    return this;
  }

  public AssertTracker noAttributes() {
    assertThat(attributes).isEmpty();
    return this;
  }

  public AssertTracker superAttribute(String key, Object value) {
    assertThat(superAttributes).containsEntry(key, value);
    return this;
  }

  public AssertTracker noSuperAttributes() {
    assertThat(superAttributes).isEmpty();
    return this;
  }

}
