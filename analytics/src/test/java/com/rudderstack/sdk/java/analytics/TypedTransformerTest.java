package com.rudderstack.sdk.java.analytics;

import com.rudderstack.sdk.java.analytics.messages.*;
import org.junit.Test;
import org.mockito.Answers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TypedTransformerTest {
  @Test
  public void messagesFanOutCorrectly() {
    MessageTransformer.Typed transformer = mock(MessageTransformer.Typed.class, Answers.CALLS_REAL_METHODS);

    AliasMessage.Builder alias = AliasMessage.builder("foo").userId("bar");
    transformer.transform(alias);
    verify(transformer).alias(eq(alias));

    GroupMessage.Builder group = GroupMessage.builder("foo").userId("bar");
    transformer.transform(group);
    verify(transformer).group(group);

    IdentifyMessage.Builder identify = IdentifyMessage.builder().userId("bar");
    transformer.transform(identify);
    verify(transformer).identify(identify);

    ScreenMessage.Builder screen = ScreenMessage.builder("foo").userId("bar");
    transformer.transform(screen);
    verify(transformer).screen(screen);

    PageMessage.Builder page = PageMessage.builder("foo").userId("bar");
    transformer.transform(page);
    verify(transformer).page(page);

    TrackMessage.Builder track = TrackMessage.builder("foo").userId("bar");
    transformer.transform(track);
    verify(transformer).track(track);
  }
}
