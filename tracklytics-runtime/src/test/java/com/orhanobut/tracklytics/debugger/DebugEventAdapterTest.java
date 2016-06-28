package com.orhanobut.tracklytics.debugger;

import android.content.Context;

import com.orhanobut.tracklytics.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class, packageName = "com.orhanobut.tracklytics")
public class DebugEventAdapterTest {

  DebugEventAdapter adapter;
  Context context;
  List<EventItem> list = spy(new ArrayList<EventItem>());
  EventItem eventItem;

  @Before public void setup() {
    eventItem = new EventItem(1, "tracker", "title", Collections.<String, Object>emptyMap());
    list.add(eventItem);
    adapter = spy(new DebugEventAdapter(list));
    context = RuntimeEnvironment.application;
  }

  @Test public void getCountShouldBe0AsDefault() {
    DebugEventAdapter adapter = new DebugEventAdapter();
    assertThat(adapter.getCount()).isEqualTo(0);
  }

  @Test public void testGetCountWithFullList() {
    assertThat(adapter.getCount()).isEqualTo(1);
  }

  @Test public void getItem() {
    assertThat(adapter.getItem(0)).isEqualTo(eventItem);
  }

  @Test public void getItemId() {
    EventItem item = new EventItem(1, "Tracker", "title", Collections.<String, Object>emptyMap());
    adapter.onEventAdded(item);

    assertThat(adapter.getItemId(1)).isEqualTo(1);
  }

  @Test public void clearAll() {
    adapter.clearAll();

    assertThat(adapter.getCount()).isEqualTo(0);
  }

  @Test public void onEventAdded() {
    EventItem item = new EventItem(1, "Tracker", "title", Collections.<String, Object>emptyMap());
    adapter.onEventAdded(item);

    verify(list).add(any(EventItem.class));
    verify(adapter).notifyDataSetChanged();
  }
}