package com.rudder.analytics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rudder.analytics.MessageTransformer;
import com.rudder.analytics.messages.AliasMessage;
import com.rudder.analytics.messages.GroupMessage;
import com.rudder.analytics.messages.IdentifyMessage;
import com.rudder.analytics.messages.PageMessage;
import com.rudder.analytics.messages.ScreenMessage;
import com.rudder.analytics.messages.TrackMessage;

import org.junit.Test;

public class TypedTransformerTest {
  @Test
  public void messagesFanOutCorrectly() {
    MessageTransformer.Typed transformer = mock(MessageTransformer.Typed.class);

    AliasMessage.Builder alias = AliasMessage.builder("foo").userId("bar");
    transformer.transform(alias);
    verify(transformer).alias(alias);

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
