package com.orhanobut.sample;

import com.orhanobut.tracklytics.TrackEvent;
import com.orhanobut.tracklytics.Tracker;
import com.orhanobut.tracklytics.TrackerAspect;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class MainPresenterTest {

  MainPresenter presenter = new MainPresenter();
  Tracker tracker;

  @Before public void setup() {
    initTracklytics();
  }

  void initTracklytics() {
    tracker = spy(new Tracker.Default().init(new TrackingAdapter() {
      @Override public void trackEvent(String title, Map<String, Object> values, Map<String, Object> superAttributes) {
        System.out.print("Test success");
      }

      @Override public void start() {

      }

      @Override public void stop() {

      }

      @Override public int id() {
        return 0;
      }
    }));
    TrackerAspect.init(tracker);
  }

  @TrackEvent("Test Event")
  @Test public void testClickEvent() {
    presenter.click();

    verify(tracker).event(anyString(), anyMap(), anyMap(), anySet());
  }
}
