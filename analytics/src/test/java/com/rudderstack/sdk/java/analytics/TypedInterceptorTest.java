package com.rudderstack.sdk.java.analytics;

import com.rudderstack.sdk.java.analytics.messages.*;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TypedInterceptorTest {
  @Test
  public void messagesFanOutCorrectly() {
    MessageInterceptor.Typed interceptor = mock(MessageInterceptor.Typed.class);

    AliasMessage alias = AliasMessage.builder("foo").userId("bar").build();
    interceptor.intercept(alias);
    verify(interceptor).alias(alias);

    GroupMessage group = GroupMessage.builder("foo").userId("bar").build();
    interceptor.intercept(group);
    verify(interceptor).group(group);

    IdentifyMessage identify = IdentifyMessage.builder().userId("bar").build();
    interceptor.intercept(identify);
    verify(interceptor).identify(identify);

    ScreenMessage screen = ScreenMessage.builder("foo").userId("bar").build();
    interceptor.intercept(screen);
    verify(interceptor).screen(screen);

    PageMessage page = PageMessage.builder("foo").userId("bar").build();
    interceptor.intercept(page);
    verify(interceptor).page(page);

    TrackMessage track = TrackMessage.builder("foo").userId("bar").build();
    interceptor.intercept(track);
    verify(interceptor).track(track);
  }
}
