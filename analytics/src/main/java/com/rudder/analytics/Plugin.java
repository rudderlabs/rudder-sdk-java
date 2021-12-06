package com.rudder.analytics;

/**
 * Plugins configure an {@link RudderAnalytics.Builder} instance. Plugins can be used to consolidate logic
 * around building an analytics client into a single class.
 */
@Beta
public interface Plugin {
  void configure(RudderAnalytics.Builder builder);
}
