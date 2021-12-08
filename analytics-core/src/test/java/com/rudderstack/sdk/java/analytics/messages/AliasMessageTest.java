package com.rudderstack.sdk.java.analytics.messages;

import com.rudderstack.sdk.java.analytics.messages.AliasMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class AliasMessageTest {

  @Test
  public void invalidPreviousIdThrows() {
    try {
      AliasMessage.builder(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("previousId cannot be null or empty.");
    }
  }

  @Test
  public void toBuilder() {
    AliasMessage original = AliasMessage.builder("previousId").userId("userId").build();
    AliasMessage copy = original.toBuilder().build();

    assertThat(copy.previousId()).isEqualTo("previousId");
  }
}
