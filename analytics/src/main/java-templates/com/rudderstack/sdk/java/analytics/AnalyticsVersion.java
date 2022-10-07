package com.rudderstack.sdk.java.analytics;

public final class AnalyticsVersion {
  private AnalyticsVersion() {
    throw new AssertionError("No instances allowed.");
  }

  public static String get() {
    return "${project.version}";
  }
}
