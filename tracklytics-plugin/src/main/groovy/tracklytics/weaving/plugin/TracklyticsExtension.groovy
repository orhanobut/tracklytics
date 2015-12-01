package tracklytics.weaving.plugin;

class TracklyticsExtension {
  boolean fabric;
  boolean mixpanel;
  boolean adjust;
  boolean snowplow;
  boolean crittercism;
  boolean googleAnalytics;

  boolean enabled() {
    return fabric || mixpanel || adjust || snowplow || crittercism || googleAnalytics;
  }
}