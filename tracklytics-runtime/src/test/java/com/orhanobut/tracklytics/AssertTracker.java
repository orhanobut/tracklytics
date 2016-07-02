package com.orhanobut.tracklytics;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

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
  private final Set<Integer> filters;

  private AssertTracker(Tracker tracker, int times) {
    initMocks(this);

    verify(tracker, atLeastOnce()).event(
        captorTrackEvent.capture(),
        captorAttributes.capture(),
        captorSuperAttributes.capture(),
        captorFilters.capture()
    );

    trackEvent = captorTrackEvent.getValue();
    attributes = captorAttributes.getValue();
    superAttributes = captorSuperAttributes.getValue();
    filters = captorFilters.getValue();
  }

  public static AssertTracker assertTrack(Tracker tracker) {
    return assertTrack(tracker, 1);
  }

  public static AssertTracker assertTrack(Tracker tracker, int times) {
    return new AssertTracker(tracker, times);
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

  public AssertTracker filters(int... filters) {
    for (int filter : filters) {
      assertThat(this.filters).contains(filter);
    }
    return this;
  }

  public AssertTracker noFilters() {
    assertThat(this.filters).isEmpty();
    return this;
  }

}
