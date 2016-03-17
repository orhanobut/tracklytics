## CHANGELOG

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
