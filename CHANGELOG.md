## CHANGELOG

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
