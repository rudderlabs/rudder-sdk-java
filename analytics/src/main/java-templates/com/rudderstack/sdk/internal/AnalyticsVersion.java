package com.rudderstack.sdk.internal;

public final class AnalyticsVersion {
  private AnalyticsVersion() {
    throw new AssertionError("No instances allowed.");
  }

  public static String get() {
    return "${project.version}";
  }
}
