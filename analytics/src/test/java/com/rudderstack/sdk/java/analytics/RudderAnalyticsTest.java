package com.rudderstack.sdk.java.analytics;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.rudderstack.sdk.java.analytics.internal.AnalyticsClient;
import com.rudderstack.sdk.java.analytics.messages.Message;
import com.rudderstack.sdk.java.analytics.messages.MessageBuilder;
import com.squareup.burst.BurstJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(BurstJUnit4.class)
public class RudderAnalyticsTest {
  @Mock AnalyticsClient client;
  @Mock
  Log log;
  @Mock
  MessageTransformer messageTransformer;
  @Mock
  MessageInterceptor messageInterceptor;
  RudderAnalytics analytics;

  @Before
  public void setUp() {
    initMocks(this);

    analytics =
        new RudderAnalytics(
            client,
            Collections.singletonList(messageTransformer),
            Collections.singletonList(messageInterceptor),
            log);
  }

  @Test
  public void enqueueIsDispatched(TestUtils.MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    Message message = messageBuilder.build();
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);
    when(messageInterceptor.intercept(any(Message.class))).thenReturn(message);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client).enqueue(message);
  }

  @Test
  public void doesNotEnqueueWhenTransformerReturnsFalse(TestUtils.MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(false);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor, never()).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test
  public void doesNotEnqueueWhenInterceptorReturnsNull(TestUtils.MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("prateek");
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);

    analytics.enqueue(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client, never()).enqueue(any(Message.class));
  }

  @Test
  public void shutdownIsDispatched() {
    analytics.shutdown();

    verify(client).shutdown();
  }

  @Test
  public void flushIsDispatched() {
    analytics.flush();

    verify(client).flush();
  }

  @Test
  public void offerIsDispatched(TestUtils.MessageBuilderTest builder) {
    MessageBuilder messageBuilder = builder.get().userId("dummy");
    Message message = messageBuilder.build();
    when(messageTransformer.transform(messageBuilder)).thenReturn(true);
    when(messageInterceptor.intercept(any(Message.class))).thenReturn(message);

    analytics.offer(messageBuilder);

    verify(messageTransformer).transform(messageBuilder);
    verify(messageInterceptor).intercept(any(Message.class));
    verify(client).offer(message);
  }
}
