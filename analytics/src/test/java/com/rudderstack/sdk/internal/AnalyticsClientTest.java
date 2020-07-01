package com.rudderstack.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.rudderstack.sdk.Callback;
import com.rudderstack.sdk.Log;
import com.rudderstack.sdk.TestUtils.MessageBuilderTest;
import com.rudderstack.sdk.http.RudderService;
import com.rudderstack.sdk.internal.AnalyticsClient;
import com.rudderstack.sdk.internal.FlushMessage;
import com.rudderstack.sdk.internal.AnalyticsClient.BatchUploadTask;
import com.rudderstack.sdk.messages.Batch;
import com.rudderstack.sdk.messages.Message;
import com.rudderstack.sdk.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.converter.ConversionException;

@RunWith(BurstJUnit4.class) //
public class AnalyticsClientTest {
  // Backo instance for testing which trims down the wait times.
  private static final Backo BACKO =
      Backo.builder().base(TimeUnit.NANOSECONDS, 1).factor(1).build();

  Log log = Log.NONE;
  ThreadFactory threadFactory;
  @Mock BlockingQueue<Message> messageQueue;
  @Mock RudderService rudderService;
  @Mock ExecutorService networkExecutor;
  @Mock Callback callback;

  @Before
  public void setUp() {
    initMocks(this);
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(
        messageQueue,
        rudderService,
        50,
        TimeUnit.HOURS.toMillis(1),
        log,
        threadFactory,
        networkExecutor,
        Collections.singletonList(callback));
  }

  @Test
  public void enqueueAddsToQueue(MessageBuilderTest builder) throws InterruptedException {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("prateek").build();
    client.enqueue(message);

    verify(messageQueue).put(message);
  }

  @Test
  public void shutdown() {
    AnalyticsClient client = newClient();

    client.shutdown();

    verify(messageQueue).clear();
    verify(networkExecutor).shutdown();
  }

  @Test
  public void flushInsertsPoison() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();

    verify(messageQueue).put(FlushMessage.POISON);
  }

  /** Wait until the queue is drained. */
  static void wait(Queue<?> queue) {
    //noinspection StatementWithEmptyBody
    while (queue.size() > 0) {}
  }

  /**
   * Verify that a {@link BatchUploadTask} was submitted to the executor, and return the {@link
   * BatchUploadTask#batch} it was uploading..
   */
  static Batch captureBatch(ExecutorService executor) {
    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(executor, timeout(1000)).submit(runnableArgumentCaptor.capture());
    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    return task.batch;
  }

  @Test
  public void flushSubmitsToExecutor() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    TrackMessage first = TrackMessage.builder("foo").userId("bar").build();
    TrackMessage second = TrackMessage.builder("qaz").userId("qux").build();
    client.enqueue(first);
    client.enqueue(second);
    client.flush();
    wait(messageQueue);

    assertThat(captureBatch(networkExecutor).batch()).containsExactly(first, second);
  }

  @Test
  public void enqueueMaxTriggersFlush() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    // Enqueuing 51 messages (> 50) should trigger flush.
    for (int i = 0; i < 51; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }
    wait(messageQueue);

    // Verify that the executor saw the batch.
    assertThat(captureBatch(networkExecutor).batch()).hasSize(50);
  }

  @Test
  public void enqueueBeforeMaxDoesNotTriggerFlush() {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    // Enqueuing 5 messages (< 50) should not trigger flush.
    for (int i = 0; i < 5; i++) {
      client.enqueue(TrackMessage.builder("Event " + i).userId("bar").build());
    }
    wait(messageQueue);

    // Verify that the executor didn't see anything.
    verify(networkExecutor, never()).submit(any(Runnable.class));
  }

  static Batch batchFor(Message message) {
    return Batch.create(Collections.<String, Object>emptyMap(), Collections.singletonList(message));
  }

  @Test
  public void batchRetriesForNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a network error 3 times.
    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
    when(rudderService.upload(batch))
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenReturn(null);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(rudderService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP5xxErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.
    Response response =
        new Response(
            "https://api.rudderlabs.com", 500, "Server Error", Collections.<Header>emptyList(), null);
    RetrofitError retrofitError = RetrofitError.httpError(null, response, null, null);
    when(rudderService.upload(batch))
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenReturn(null);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(rudderService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP429Errors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.
    Response response =
        new Response(
            "https://api.rudderlabs.com", 429, "Rate Limited", Collections.<Header>emptyList(), null);
    RetrofitError retrofitError = RetrofitError.httpError(null, response, null, null);
    when(rudderService.upload(batch))
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenThrow(retrofitError)
        .thenReturn(null);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    verify(rudderService, times(4)).upload(batch);
    verify(callback).success(trackMessage);
  }

  @Test
  public void batchDoesNotRetryForNon5xxAndNon429HTTPErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error that should not be retried.
    Response response =
        new Response(
            "https://api.rudderlabs.com", 404, "Not Found", Collections.<Header>emptyList(), null);
    RetrofitError retrofitError = RetrofitError.httpError(null, response, null, null);
    doThrow(retrofitError).when(rudderService).upload(batch);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(rudderService).upload(batch);
    verify(callback).failure(trackMessage, retrofitError);
  }

  @Test
  public void batchDoesNotRetryForNonNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);
    RetrofitError retrofitError =
        RetrofitError.conversionError(null, null, null, null, new ConversionException("fake"));
    doThrow(retrofitError).when(rudderService).upload(batch);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    verify(rudderService).upload(batch);
    verify(callback).failure(trackMessage, retrofitError);
  }

  @Test
  public void givesUpAfterMaxRetries() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);
    RetrofitError retrofitError = RetrofitError.networkError(null, new IOException());
    when(rudderService.upload(batch)).thenThrow(retrofitError);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch);
    batchUploadTask.run();

    // 50 == MAX_ATTEMPTS in AnalyticsClient.java
    verify(rudderService, times(50)).upload(batch);
    verify(callback)
        .failure(
            eq(trackMessage),
            argThat(
                new TypeSafeMatcher<Throwable>() {
                  @Override
                  public void describeTo(Description description) {
                    description.appendText("expected IOException");
                  }

                  @Override
                  protected boolean matchesSafely(Throwable item) {
                    IOException exception = (IOException) item;
                    return exception.getMessage().equals("50 retries exhausted");
                  }
                }));
  }
}
