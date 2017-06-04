[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Tracklytics-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2891) [![](https://img.shields.io/badge/AndroidWeekly-%23183-blue.svg)](http://androidweekly.net/issues/issue-183)

# Tracklytics
We all use analytics tools to provide a better user experience. (Mixpanel, Firebase, Fabric etc). I call this concept as tracking.
Tracking events are cross-cutting and boiler plate most of the time. Tracklytics abstracts away all tracking events into annotations.

Events have a common pattern, and almost all of them have the following structure:

1. Event name 
2. Event metadata as attributes (key/value pair)

## How to use it
#### Download
Add the following code block to in your app/build.gradle. 
```groovy
buildscript {
  dependencies {
    classpath 'com.orhanobut.tracklytics:tracklytics-plugin:2.0.0'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'com.orhanobut.tracklytics'   // Must be added after com.android.application

```

#### Initiate
Subscribe to all tracked events and send them to your preferred analytic tools.
```java
Tracklytics.init(new EventSubscriber() {
  @Override public void onEventTracked(Event event) {
    // Send your events to Mixpanel, Fabric etc
  }
});
```

#### Use
```java
@TrackEvent("event_name") 
public void foo() {
}
```

## Guideline

### @TrackEvent
_**Scope:** Method_

Track an event and notify the subscriber(EventSubscriber) on each method invocation.
```java
@TrackEvent("event_name") 
public void foo() {
}
```

### @Attribute
_**Scope:** Method, Method parameters_

There are multiple ways to add an attribute to the corresponding event. Assigns the values in runtime dynamically.

By using method parameters: Parameter value will be used as attribute value.
```java
@TrackEvent("event_name") 
public void foo(@Attribute("attribute_key") String name) {
  // something
}
```

By using the return value of the method as attribute value. 
```java
@TrackEvent("event_name") 
@Attribute("attribute_key")
public String foo() {
  // something
  return "attribute_value";
}
```

Set a default value when the expected value is null.
```java
@TrackEvent("event_name") 
public void foo(@Attribute(value="attribute_key", defaultValue="defaultValue") String name) {
  // something
}
```

### @FixedAttribute
_**Scope:** Method, Class_

If the attribute values are constant, use FixedAttribute. 

On method: Only this event will have this fixed attribute.
```java
@TrackEvent("eventName")
@FixedAttribute(key="Login", value="Success")
public void foo(){
}
```

On class: These attributes will be added to each event that is triggered within this class. 
For example: Following `foo()` method will also have `screen_name` attribute.
```java
@FixedAttribute(key="screen_name", value="Login")
public class LoginPresenter{

  @TrackEvent("login")
  public void onLoggedIn(){
  }
}
```

### @FixedAttributes
_**Scope:** Method, Class_

Prior to Java 8, repeated annotations are not available. Sometimes you may need more fixed attributes. Use this annotation to add multiple attributes
```java
@TrackEvent("event_name")
@FixedAttributes({
  @FixedAttribute(key="name", value="Something"),
  @FixedAttribute(key="last_name", value="Something")
})
public void foo(){
}
```

### Filters
Use filters to differentiate some events. You may only want to send specific events to a specific analytics tool. 
ie: Send login event to Fabric.
```java
@TrackEvent(value="event_name",filters=100)
public void foo() {
}
```

### Tags
You can use tags to send more information about the tracked event. For example: Adjust requires token for their events.
```java
@TrackEvent(value="event",filters=100, tags="abc123")
public void trackNoValues() {
}
```

### Super Attributes
Some attributes might be used for every event within the app such as device id.
Tracklytics call them as super attributes. These attributes will be automatically added to each event.
You only need to set them once and Tracklytics will do the rest.

Access super attributes via Event class
```java
Tracklytics.init(new EventSubscriber() {
  @Override public void onEvent(Event event) {
    // event.superAttributes
  }
});
```

Set any attribute as super
```java
@Attribute(value="key", isSuper=true)
```

Set any fixed attribute as super
```java
@FixedAttribute(key="key", value="value",  isSuper=true)
```

Add super attribute directly
```java
tracklytics.addSuperAttribute("key","value");
```

Remove super attribute
```java
tracklytics.removeSuperAttribute("key");
```

### @TrackableAttribute / @Trackable
You can make the class trackable in order to provide preset values. 
Imagine your domain models, they contain a lot of information and you can re-use them as a source for tracking.
For the following use case: When `event` is triggered, attributes from `Foo.getTrackableAttributes()` will be added to this event.

```java
class Foo implements Trackable {
  String name;
  
  @Override public Map<String, String> getTrackableAttributes() {
    Map<String,String> values = new HashMap<>();
    values.put("name", name);
    return values;
  }
}
```
```java
@TrackEvent("event")
void something(@TrackableAttribute FooTrackable foo){
}
```

### @TransformAttribute / @TransformAttributeMap
Sometimes values are not in the correct form, such as position or index is in Integer type. 
This might not be clear if you track them as raw.
You may want to send a more understandable value to the analytic tools.
Use TransformAttribute to solve this issue.

For example: In the following example, index is represented by integer and you want to have a String value which represent exact value such as menu item.

```java
class Foo {
  @TrackEvent("event")
  @TransformAttributeMap(
    keys = {0, 1},
    values = {"value0", "value1"}
  )
  public void foo(@TransformAttribute("key") int index) {
  }
}

// foo(0) : event -> [{"key","value0}]
// foo(1) : event -> [{"key","value1}]
```

### Log
Tracklytics provides a stream for logs which sends formatted event messages.
```java
Tracker tracklytics = Tracker.init(...);
tracklytics.setEventLogListener(new EventLogListener() {
  @Override public void log(String message) {
    // Set your logger here. ie: Logger or Timber
    Log.d("Tracker", message);
  }
});
```
Log output sample
```
eventName:{key=value}, super attrs: {key=value}, tags={100,200}
```

### More API options
You can also track event directly without annotations.
```java
tracklytics.trackEvent(String eventName)

tracklytics.trackEvent(String eventName, Map attributes)
```

### Event Debugging Monitor
Use [Bee](https://github.com/orhanobut/bee) to monitor your events

### How it works
<img src='https://github.com/orhanobut/tracklytics/blob/master/art/how_it_works.png'/>

### Licence
<pre>
Copyright 2017 Orhan Obut

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
