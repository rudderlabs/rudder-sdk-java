package com.rudderstack.sdk;

import com.rudderstack.sdk.Beta;

/**
 * Plugins configure an {@link Analytics.Builder} instance. Plugins can be used to consolidate logic
 * around building an analytics client into a single class.
 */
@Beta
public interface Plugin {
  void configure(Analytics.Builder builder);
}
