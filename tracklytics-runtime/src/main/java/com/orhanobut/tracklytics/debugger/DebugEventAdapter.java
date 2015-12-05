package com.orhanobut.tracklytics.debugger;

import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orhanobut.tracklytics.R;
import com.orhanobut.tracklytics.TrackEventSubscriber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebugEventAdapter extends BaseAdapter implements TrackEventSubscriber {

  private final List<EventItem> list;

  public DebugEventAdapter() {
    this(new ArrayList<EventItem>());
  }

  public DebugEventAdapter(List<EventItem> list) {
    this.list = list;
  }

  @Override public int getCount() {
    return list.size();
  }

  @Override public EventItem getItem(int position) {
    return list.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    View view = convertView;

    if (view == null) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      view = inflater.inflate(R.layout.tracklytics_debugger_list_item, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.tracker = (TextView) view.findViewById(R.id.tracklytics_debugger_tracker);
      viewHolder.eventName = (TextView) view.findViewById(R.id.tracklytics_debugger_eventName);
      viewHolder.eventValues = (TextView) view.findViewById(R.id.tracklytics_debugger_values);
      viewHolder.date = (TextView) view.findViewById(R.id.tracklytics_debugger_date);

      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }

    EventItem item = getItem(position);
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    viewHolder.date.setText(dateFormat.format(item.eventDate));
    viewHolder.eventName.setText(item.eventName);
    viewHolder.tracker.setText(item.trackerName);
    viewHolder.eventValues.setText(item.eventValues.toString());

    return view;
  }

  public void clearAll() {
    list.clear();
    notifyDataSetChanged();
  }

  static class ViewHolder {
    TextView tracker;
    TextView eventName;
    TextView eventValues;
    TextView date;
  }

  @Override public void onEventAdded(EventItem item) {
    list.add(0, item);
    notifyDataSetChanged();
  }
}
