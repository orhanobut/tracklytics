## CHANGELOG

### 1.1.24-SNAPSHOT
- Tracker.addSuperAttribute allows you to add any super attribute without requiring annotation
```java
tracker.addSuperAttribute("key", "value");
```

### 1.1.23-SNAPSHOT
- TrackSuperAttribute annotation added. It can be used to set super attributes without TrackEvent.
```java
@TrackSuperAttribute
public void foo(@Attribute("key") String value){
}
```

### 1.1.20-SNAPSHOT
- Trackable Map<String,String> second type changed to Object. (Map<String,Object>)

### 1.1.19-SNAPSHOT
- tags type changed from int to String
- filters parameter added to TrackEvent annotation as optional

### 1.1.18-SNAPSHOT
- Tracker.init bug fix

### 1.1.17-SNAPSHOT
- TrackFilter is removed
- Small performance improvement
- TrackEvent now accepts "tags". You can use it for different purposes. For instance: Tracking filter
```java
@TrackEvent(value="event", tags={100,200})
public void foo(){}
```
- TrackingAdapter.trackEvent signature changed. It supports "tags" now.

### 1.1.16-SNAPSHOT
- ScreenNameAttribute annotation added. You can use this annotation to use the class name as value
- Tracklytics init is changed. There is no backward compatibility unfortunately. Use the following approach:
```java
Tracker.init(trackingAdapters);
```
- TracklyticsLogger interface added for logging.
```java
Tracker tracker = Tracker.init(trackingAdapters);
tracker.setLogger(new TracklyticsLogger(){
   @Override public void log(String message){
     // User your favorite log tool to log the message
   }
});
```

### 1.1.13-SNAPSHOT
- Super class methods now can be used for tracking for the subclasses
```java
class Base {

  @TrackEvent("event"
  public void init(){}
}

@FixedAttribute(key="key", value="value")
class Foo extends Base {
}

new Foo().init();

//OUTPUT:  event->{key=value}
```

### 1.1.12-SNAPSHOT
- FixedAttribute(s) declared on method will override class-wide FixedAttribute(s).

### 1.1.11-SNAPSHOT
- Bug fix: TrackEvent will only use Trackable.getTrackableAttributes() when TrackableAttribute presents

### 1.1.10-SNAPSHOT
- TrackableAttribute for current class NPE fix

### 1.1.9-SNAPSHOT
- TrackableAttribute is available for the current class now.
```java
class Foo implements Trackable {

  @Override public Map<String, String> getTrackableAttributes() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    return map;
  }

  @TrackEvent("event")
  @TrackableAttribute
  public void foo() {
  }
}

//OUTPUT: event->[{"key","value"}]
```

### 1.1.8-SNAPSHOT
- TransformAttributeMap keys type changed to int intead of String

### 1.1.7-SNAPSHOT
- When Attribute is used for method parameters, isSuper and defaultValue are properly working now.
- TransformAttribute have isSuper and defaultValue
- TransformAttribute is available for method return value.

### 1.1.6-SNAPSHOT
- New: TransformAttribute and TransformAttributeMap added. Sometimes you might have some parameter values which
represents another value or they are integer or enum. You may need to have the corresponding value in tracking.
TransformAttribute helps you in this case. For example: In the following example, position is represented by integer
and you want to have a String value which represent exact value such as menu item.

```java
class Foo {
  @TrackEvent("event")
  @TransformAttributeMap(
    keys = {0, 1},
    values = {"value0", "value1"}
  )
  public void foo(@TransformAttribute("key") int position) {
  }
}

// foo(0) : event -> [{"key","value0}]
// foo(1) : event -> [{"key","value1}]
```

### 1.1.5-SNAPSHOT
- Enclosing class attributes will be added to inner or anonymous class events.
```java
@FixedAttribute(key="key1", value="value1")
public class Enclosing {
 
  @FixedAttribute(key="key2", value="value2")
  static class Inner {
  
    @TrackEvent("Event")
    public void bar(){
    }
  }
}

// Output: Event : [{key1,value1}, {key2,value2}]
```

### 1.1.4-SNAPSHOT
- Bug fix: Method parameters without annotation caused to crash. Fixed.

### 1.1.1-SNAPSHOT
- TrackableAttribute annotation is added. Works with Trackable types.
- Trackable type added. Types can have predefined attributes now.

```java
class Foo implements Trackable {
  @Override public Map<String, String> getTrackableAttributes() {
    Map<String,String> values = new HashMap<>();
    values.put("key","value");
    return values;
  }
}

@TrackEvent("Event A")
void something(@TrackableAttribute FooTrackable foo){}
```
When Event A is triggered, Foo.getTrackableAttributes() will be added to this event.

### 1.0.1-SNAPSHOT
- Attribute signature changed
- FixedAttribute added
- FixedAttributes added to allow repeated attributes
- Class-wide attributes added (FixedAttribute)
- App-wide (super attributes) added. (isSuper)
- All analytic implementations are removed. Completely decoupled from them
- TrackingAdapter signature changed
- Gradle extension is removed. No need to call execute thingy anymore. (It was quite ugly solution)

### Version 0.22-SNAPSHOT

Following trackers are removed for the time being. The reason was inconsistency for the usage. They will be added
one by one with more care.
- Google analytics is removed
- Snowplow is removed
- Adjust is removed
- Crittercism is removed

New: Track option added. For single tracking operation, you can use Track.
```java
@Track(eventName="Foo", attributeKey="Login", attributeValue="Success")
void something(){}
```

New: TrackValue name changed to Attribute and it has now a default result
```java
@TrackValue(value="Login", defaultResult="Success")
```
