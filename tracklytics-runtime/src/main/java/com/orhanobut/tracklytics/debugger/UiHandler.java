package com.orhanobut.tracklytics.debugger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.orhanobut.tracklytics.R;

public final class UiHandler {

  private Activity context;

  private final ImageView beeImageView;
  private final Point displaySize;

  private View container;
  private DebugEventAdapter adapter;
  private int buttonSize;

  public UiHandler(Activity activity) {
    context = activity;

    beeImageView = new ImageView(context);

    Display display = activity.getWindowManager().getDefaultDisplay();
    displaySize = new Point();
    displaySize.set(display.getWidth(), display.getHeight());

    init();

    buttonSize = dpToPx(24, context);
  }

  public int dpToPx(int dp, Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    return px;
  }

  private void init() {
    View rootView = context.getWindow().getDecorView();
    LayoutInflater inflater = LayoutInflater.from(context);
    View mainContainer = inflater.inflate(R.layout.tracklytics_debugger_layout, (ViewGroup) rootView, true);
    container = mainContainer.findViewById(R.id.tracklytics_debugger_container);
    container.setVisibility(View.GONE);
    mainContainer.findViewById(R.id.tracklytics_debugger_close).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        container.setVisibility(View.GONE);
        beeImageView.setVisibility(View.VISIBLE);
      }
    });
    mainContainer.findViewById(R.id.tracklytics_debugger_done_all).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        adapter.clearAll();
      }
    });

    ListView listView = (ListView) container.findViewById(R.id.tracklytics_debugger_list);

    adapter = new DebugEventAdapter(EventQueue.getUndispatched());
    listView.setAdapter(adapter);

    EventQueue.subscribe(adapter);
  }

  public void inject() {
    ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
    setButton(rootView);
  }

  private void setButton(ViewGroup rootView) {
    beeImageView.setImageResource(R.drawable.ic_pan_tool_black_24dp);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER | Gravity.RIGHT
    );

    beeImageView.setLayoutParams(params);
    beeImageView.setOnClickListener(null);

    beeImageView.setOnTouchListener(onTouchListener);
    rootView.addView(beeImageView);
  }

  private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {

    private static final int MIN_MOVEMENT = 20;

    final GestureListener gestureListener = new GestureListener();
    final GestureDetector gestureDetector = new GestureDetector(context, gestureListener);

    PointF touchPos = new PointF();
    long touchTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (gestureDetector.onTouchEvent(event)) {
        return true;
      }
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          touchTime = SystemClock.uptimeMillis();
          touchPos.set(event.getX(), event.getY());
          break;
        case MotionEvent.ACTION_MOVE:
          int x = (int) event.getRawX();
          int y = (int) event.getRawY();

          if (!isMoveable(x, y)) {
            break;
          }
          if (!isInBoundaries(x, y)) {
            break;
          }

          FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
          params.topMargin = y - v.getHeight() / 2;
          params.leftMargin = x - v.getWidth() / 2;
          params.gravity = Gravity.NO_GRAVITY;
          v.setLayoutParams(params);
          break;
      }
      return SystemClock.uptimeMillis() - touchTime > 200;
    }

    private boolean isMoveable(int x, int y) {
      if (SystemClock.uptimeMillis() - touchTime < 200) {
        return false;
      }
      return (Math.abs(x - touchPos.x) > MIN_MOVEMENT || Math.abs(y - touchPos.y) > MIN_MOVEMENT);
    }

    private boolean isInBoundaries(int x, int y) {
      int half = buttonSize / 2;
      return !(x + half > displaySize.x || x < half || y + half > displaySize.y || y < half + 50);
    }

  };

  private class GestureListener implements GestureDetector.OnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      beeImageView.setVisibility(View.GONE);
      container.setVisibility(View.VISIBLE);
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }

}
