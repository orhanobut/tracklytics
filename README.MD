# Tracklytics

Tracklytics is an annotation based analytic tools aggregator with using AOP to track events and crashes. It basically collects all analytic/tracking tools together and provide a simple and clean solution without polluting your code base.

Common problems are for the analytics tools are;
  - Polluted code, more tracking code than the business code
  - Too many analytic tools to track events
  - Hard to maintain

Tracklytics moves all these problems to an individual module which has handler for each analytic tool. By using aspect oriented programming, all tracking codes will be added to your business code during the compile time, which means that you don't need to worry about performance.

## Tracklytics solution
- Moves all analytics/tracking code and add them in compile time.
- Provides a debugging monitor tool to visualise all occured events.

<img src='https://github.com/orhanobut/tracklytics/blob/master/art/image4.png'/>

# Usage

#### @TrackEvent("EventName")
Track an event by using event name. 
```java
@TrackEvent("title") void trackNoValues() {
  // something
}
```

#### @TrackValue("key")
Track method parameters as key/value pair.
```java
@TrackEvent("eventName") void trackMe(@TrackValue("eventKey") String eventValue) {
  // something
}
```

Track event with return value. By adding @TrackValue to the method, it will add the returning value to the event key/value pairs.
```java
@TrackEvent("eventName") @TrackValue("eventKey") String trackMe() {
  // something
  return "eventValue";
}
```

Track event with both return value and parameters.
```java
@TrackEvent("eventName") @TrackValue("eventKey1")
String trackMe(@TrackValue("eventKey2") String eventValue2) {
  // something
  return "eventValue1";
}
```

### @TrackFilter(TRACKER_TYPE1, TRACKER_TYPE2)
Sometimes you may want to track an event for a specific tracker or trackers. Filter will handle it for you. For example, below example: Only google analytics will track the event.
```java
@TrackFilter(TrackerType.GOOGLE_ANALYTICS)
@TrackEvent("title") void trackEventFilter() {
  // something
}
```

### @Tracklytics

Before using tracklytics, you must initialize it. Create a method which returns Tracker type and initialize all your trackers. All dependencies and other complex logic will be added/handled by tracklytics.
```java
class DefaultApplication {

  @Tracklytics(TrackerAction.INIT) Tracker init() {
    return Tracker.init(
        new MixPanelTrackingAdapter(context, "API_KEY"),
        new GoogleAnalyticsTrackingAdapter(context, "CONTAINER_ID", R.raw.container),
        new CrittercismTrackingAdapter(context, "APP_ID"),
        new AdjustTrackingAdapter(context, "APPTOKEN", AdjustTrackingAdapter.Environment.LIVE),
        new FabricTrackingAdapter(context)
    );
  }
}
```

For the best usage, use start and stop functions in your activities onStart/onStop or on any other entry/exit points
```java
class MainActivity extends Activity {

  @Tracklytics(TrackerAction.START) 
  @Override void onStart(){
  }
  
  @Tracklytics(TrackerAction.STOP) 
  @Override void onStop(){
  }
  
}
```

## Install
Add the following code block to in your app/build.gradle, since it's in progress, artifacts are in snapshot. Also have some issues to move all related fabric stuff to the tracklytics module. Because of that you need to add it to the classpath.
```groovy
buildscript {
  repositories {
    jcenter()
    maven { url 'https://maven.fabric.io/public' }
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
  }
  dependencies {
    classpath 'io.fabric.tools:gradle:1.+'
    classpath 'com.orhanobut.tracklytics:tracklytics-plugin:0.8-SNAPSHOT'
  }
}

repositories {
  jcenter()
  maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}

apply plugin: 'com.android.application'
// Make sure that you add this after com.android.application
apply plugin: 'com.orhanobut.tracklytics'
```

Enable the trackers that you want to use, as default they are enabled. Only enabled tracker's dependencies will be added to the project.
```groovy
  tracklytics {
    mixpanel = true
    adjust = false
    fabric = false
    snowplow = false
    googleAnalytics = false
    crittercism = false
  }
  startTracklytics.execute()
```

### Event Debugging Monitor
<img src='https://github.com/orhanobut/tracklytics/blob/master/art/image1.png' height='400'/> <img src='https://github.com/orhanobut/tracklytics/blob/master/art/image2.png' height='400'/>
To be able to use debugging monitor, you need to inject it in the activity class.
```java
 TracklyticsDebugger.inject(this);
```

By clicking "hand" icon, monitor will be displayed and each event will be updated in the list.

Debugging monitor displays
- each event for each tracker
- an option to remove all
- filter option to select tracker, time and keyword (in progress)

### Add custom tracker
You can always add another tracker by using TrackingAdapter.

Currently available tools:

- Google analytics
- Fabric (Crashlytics)
- Crittercism
- Mixpanel
- Adjust
- SnowPlow (in progress)

##TOO
- put the artifacts into the release repository and minify install part
- find a solution for fabric
- add more analytic tools

### Licence
<pre>
Copyright 2015 Orhan Obut

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
