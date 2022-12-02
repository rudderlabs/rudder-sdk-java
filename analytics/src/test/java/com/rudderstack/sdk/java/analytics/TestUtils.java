package com.rudderstack.sdk.java.analytics;

import com.rudderstack.sdk.java.analytics.messages.*;

public final class TestUtils {
  private TestUtils() {
    throw new AssertionError("No instances.");
  }

  @SuppressWarnings("UnusedDeclaration")
  public enum MessageBuilderTest {
    ALIAS {
      @Override
      public AliasMessage.Builder get() {
        return AliasMessage.builder("foo");
      }
    },
    GROUP {
      @Override
      public GroupMessage.Builder get() {
        return GroupMessage.builder("foo");
      }
    },
    IDENTIFY {
      @Override
      public IdentifyMessage.Builder get() {
        return IdentifyMessage.builder();
      }
    },
    SCREEN {
      @Override
      public ScreenMessage.Builder get() {
        return ScreenMessage.builder("foo");
      }
    },
    TRACK {
      @Override
      public TrackMessage.Builder get() {
        return TrackMessage.builder("foo");
      }
    };

    public abstract <T extends Message, V extends MessageBuilder> MessageBuilder<T, V> get();
  }
}