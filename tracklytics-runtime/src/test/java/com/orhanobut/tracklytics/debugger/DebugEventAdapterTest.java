package com.orhanobut.tracklytics.debugger;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.orhanobut.tracklytics.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class DebugEventAdapterTest {

  private DebugEventAdapter adapter;
  private Context context;
  private List<EventItem> list = spy(new ArrayList<EventItem>());

  @Before public void setup() {
    EventItem item = new EventItem(1, "tracker", "title", Collections.<String, Object>emptyMap());
    list.add(item);
    adapter = spy(new DebugEventAdapter(list));
    context = Robolectric.setupActivity(Activity.class).getApplicationContext();
  }

  @Test public void getCountShouldBe0AsDefault() throws Exception {
    DebugEventAdapter adapter = new DebugEventAdapter();
    assertThat(adapter.getCount()).isEqualTo(0);
  }

  @Test public void testGetCountWithFullList() throws Exception {
    assertThat(adapter.getCount()).isEqualTo(1);
  }

  @Test public void getItem() throws Exception {
    assertThat(adapter.getItem(0)).isNotNull();
  }

  @Test public void getItemId() throws Exception {
    EventItem item = new EventItem(1, "Tracker", "title", Collections.<String, Object>emptyMap());
    adapter.onEventAdded(item);

    assertThat(adapter.getItemId(1)).isEqualTo(1);
  }

  @Test public void getView() throws Exception {
    View view = adapter.getView(0, null, new LinearLayout(context));

    assertThat(view).isNotNull();
    assertThat(view.getTag()).isNotNull();
  }

  @Test public void viewTagShouldHaveViewHolder() {
    View view = adapter.getView(0, null, new LinearLayout(context));

    assertThat(view.getTag()).isInstanceOf(DebugEventAdapter.ViewHolder.class);
  }

  @Test public void clearAll() throws Exception {
    adapter.clearAll();

    assertThat(adapter.getCount()).isEqualTo(0);
  }

  @Test public void onEventAdded() throws Exception {
    EventItem item = new EventItem(1, "Tracker", "title", Collections.<String, Object>emptyMap());
    adapter.onEventAdded(item);

    verify(list).add(any(EventItem.class));
    verify(adapter).notifyDataSetChanged();
  }
}