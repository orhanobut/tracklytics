# Tracklytics

Tracklytics is an annotation based analytic tools aggregator with using AOP to track events and crashes. It basically collects all analytic/tracking tools together and provide a simple and clean solution without polluting your code base.

Common problems are for the analytics tools are;
  - Polluted code, more tracking code than the business code
  - Too many analytic tools to track events
  - Hard to maintain

Tracklytics moves all these problems to an individual module which has handler for each analytic tool. By using aspect oriented programming, all tracking codes will be added to your business code during the compile time, which means that you don't need to worry about performance.

# Usage

#### @TrackEvent("EventName")
Track an event by using event name. 
```java
class MainActivity {

  @TrackEvent("title") void trackNoValues() {
  }
}
```

#### @TrackValue("key")
Track method parameters as key/value pair.
```java
class MainActivity {

  @TrackEvent("eventName") void trackMe(@TrackValue("eventKey") String eventValue) {
  }
  
}
```

Track event with return value. By adding @TrackValue to the method, it will add the returning value to the event key/value pairs.
```java
class MainActivity {

  @TrackEvent("eventName") @TrackValue("eventKey") String trackMe() {
    return "eventValue";
  }
  
}
```

Track event with both return value and parameters.
```java
class MainActivity {

  @TrackEvent("eventName") @TrackValue("eventKey1")
  String trackMe(@TrackValue("eventKey2") String eventValue2) {
    return "eventValue1";
  }
  
}
```

### @TrackFilter(TRACKER_TYPE1, TRACKER_TYPE2)
Sometimes you may want to track an event for a specific tracker or trackers. Filter will handle it for you. For example, below example: Only google analytics will track the event.
```java
class MainActivity {

  @TrackFilter(TrackerType.GOOGLE_ANALYTICS)
  @TrackEvent("title") void trackEventFilter() {
  }
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
    classpath 'com.orhanobut.tracklytics:tracklytics-plugin:0.7-SNAPSHOT'
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
