package com.rudderstack.sdk.java.analytics.internal;

import static com.rudderstack.sdk.java.analytics.internal.StopMessage.STOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.MockitoAnnotations.openMocks;

import com.rudderstack.sdk.java.analytics.Callback;
import com.rudderstack.sdk.java.analytics.Log;
import com.rudderstack.sdk.java.analytics.TestUtils.MessageBuilderTest;
import com.rudderstack.sdk.java.analytics.http.RudderService;
import com.rudderstack.sdk.java.analytics.http.UploadResponse;
import com.rudderstack.sdk.java.analytics.internal.AnalyticsClient;
import com.rudderstack.sdk.java.analytics.internal.AnalyticsClient.BatchUploadTask;
import com.rudderstack.sdk.java.analytics.internal.FlushMessage;
import com.rudderstack.sdk.java.analytics.messages.Batch;
import com.rudderstack.sdk.java.analytics.messages.Message;
import com.rudderstack.sdk.java.analytics.messages.TrackMessage;
import com.segment.backo.Backo;
import com.squareup.burst.BurstJUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

@RunWith(BurstJUnit4.class) //
public class RudderAnalyticsClientTest {
  // Backo instance for testing which trims down the wait times.
  private static final Backo BACKO =
      Backo.builder().base(TimeUnit.NANOSECONDS, 1).factor(1).build();

  private static String SUCCESS_RESPONSE = "OK";
  private int DEFAULT_RETRIES = 10;
  private int MAX_BYTE_SIZE = 1024 * 500; // 500kb

  Log log = Log.NONE;

  ThreadFactory threadFactory;
  @Spy LinkedBlockingQueue<Message> messageQueue /*= new LinkedBlockingQueue<>()*/;
  @Mock RudderService rudderService;
  @Mock ExecutorService networkExecutor;
  @Mock
  Callback callback;

  AtomicBoolean isShutDown;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    isShutDown = new AtomicBoolean(false);
//    messageQueue = spy(new LinkedBlockingQueue<Message>());
    threadFactory = Executors.defaultThreadFactory();
  }

  // Defers loading the client until tests can initialize all required
  // dependencies.
  AnalyticsClient newClient() {
    return new AnalyticsClient(
        messageQueue,
        rudderService,
        50,
        TimeUnit.HOURS.toMillis(1),
        0,
        MAX_BYTE_SIZE,
        log,
        threadFactory,
        networkExecutor,
        Collections.singletonList(callback),
        isShutDown);
  }

  @Test
  public void enqueueAddsToQueue(MessageBuilderTest builder) throws InterruptedException {
    AnalyticsClient client = newClient();

    Message message = builder.get().userId("Deba").build();
    client.enqueue(message);

    Mockito.verify(messageQueue).put(message);
  }

  @Test
  public void shutdown() throws InterruptedException {
    messageQueue = new LinkedBlockingQueue<>();
    AnalyticsClient client = newClient();

    client.shutdown();

    Mockito.verify(networkExecutor).shutdown();
    Mockito.verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
  }

  @Test
  public void flushInsertsPoison() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();

    Mockito.verify(messageQueue).put(FlushMessage.POISON);
  }

  /** Wait until the queue is drained. */
  static void wait(Queue<?> queue) {
    // noinspection StatementWithEmptyBody
    while (queue.size() > 0) {}
  }

  /**
   * Verify that a {@link BatchUploadTask} was submitted to the executor, and return the {@link
   * BatchUploadTask#batch} it was uploading..
   */
  static Batch captureBatch(ExecutorService executor) {
    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    Mockito.verify(executor, Mockito.timeout(1000)).submit(runnableArgumentCaptor.capture());
    final BatchUploadTask task = (BatchUploadTask) runnableArgumentCaptor.getValue();
    return task.batch;
  }

  private static String generateMassDataOfSize(int msgSize) {
    char[] chars = new char[msgSize];
    Arrays.fill(chars, 'a');

    return new String(chars);
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
  public void shouldBeAbleToCalculateMessageSize() {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property1", generateMassDataOfSize(1024 * 33));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    // can't test for exact size cause other attributes come in play
    Assertions.assertThat(client.messageSizeInBytes(bigMessage)).isGreaterThan(1024 * 33);
  }

  @Test
  public void dontFlushUntilReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property2", generateMassDataOfSize(MAX_BYTE_SIZE - 200));

    TrackMessage bigMessage =
        TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
    client.enqueue(bigMessage);

    wait(messageQueue);

    Mockito.verify(networkExecutor, Mockito.never()).submit(ArgumentMatchers.any(Runnable.class));
  }

  @Test
  public void flushWhenReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE));

    for (int i = 0; i < 10; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    Mockito.verify(networkExecutor, Mockito.times(10)).submit(ArgumentMatchers.any(Runnable.class));
  }

  @Test
  public void flushHowManyTimesNecessaryToStayWithinLimit() throws InterruptedException {
    AnalyticsClient client =
        new AnalyticsClient(
            messageQueue,
            rudderService,
            50,
            TimeUnit.HOURS.toMillis(1),
            0,
            MAX_BYTE_SIZE * 4,
            log,
            threadFactory,
            networkExecutor,
            Collections.singletonList(callback),
            isShutDown);

    Map<String, String> properties = new HashMap<String, String>();

    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE));

    for (int i = 0; i < 4; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    Mockito.verify(networkExecutor, Mockito.times(4)).submit(ArgumentMatchers.any(Runnable.class));
  }

  @Test
  public void flushWhenMultipleMessagesReachesMaxSize() throws InterruptedException {
    AnalyticsClient client = newClient();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("property3", generateMassDataOfSize(MAX_BYTE_SIZE / 9));

    for (int i = 0; i < 10; i++) {
      TrackMessage bigMessage =
          TrackMessage.builder("Big Event").userId("bar").properties(properties).build();
      client.enqueue(bigMessage);
    }

    wait(messageQueue);

    Mockito.verify(networkExecutor, Mockito.times(1)).submit(ArgumentMatchers.any(Runnable.class));
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
    Mockito.verify(networkExecutor, Mockito.never()).submit(ArgumentMatchers.any(Runnable.class));
  }

  static Batch batchFor(Message message) {
    return Batch.create(Collections.<String, Object>emptyMap(), Collections.singletonList(message));
  }

  @Test
  public void batchRetriesForNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Response<ResponseBody> successResponse = Response.success(200, ResponseBody.create(SUCCESS_RESPONSE, MediaType.parse("text/plain")));
    Response<ResponseBody> failureResponse = Response.error(429, ResponseBody.create("", null));

    // Throw a network error 3 times.
    Mockito.when(rudderService.upload(batch))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(failureResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    Mockito.verify(rudderService, Mockito.times(4)).upload(batch);
    Mockito.verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP5xxErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.

    Response<ResponseBody> successResponse = Response.success(200, ResponseBody.create(SUCCESS_RESPONSE, MediaType.parse("text/plain")));
    Response<ResponseBody> failResponse =
        Response.error(500, ResponseBody.create(null, "Server Error"));
    Mockito.when(rudderService.upload(batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    Mockito.verify(rudderService, Mockito.times(4)).upload(batch);
    Mockito.verify(callback).success(trackMessage);
  }

  @Test
  public void batchRetriesForHTTP429Errors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error 3 times.
    Response<ResponseBody> successResponse = Response.success(200, ResponseBody.create(SUCCESS_RESPONSE, MediaType.parse("text/plain")));
    Response<ResponseBody> failResponse =
        Response.error(429, ResponseBody.create(null, "Rate Limited"));
    Mockito.when(rudderService.upload(batch))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(failResponse))
        .thenReturn(Calls.response(successResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify that we tried to upload 4 times, 3 failed and 1 succeeded.
    Mockito.verify(rudderService, Mockito.times(4)).upload(batch);
    Mockito.verify(callback).success(trackMessage);
  }

  @Test
  public void batchDoesNotRetryForNon5xxAndNon429HTTPErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    // Throw a HTTP error that should not be retried.
    Response<ResponseBody> failResponse =
        Response.error(404, ResponseBody.create(null, "Not Found"));
    Mockito.when(rudderService.upload(batch)).thenReturn(Calls.response(failResponse));

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    Mockito.verify(rudderService).upload(batch);
    Mockito.verify(callback).failure(ArgumentMatchers.eq(trackMessage), ArgumentMatchers.any(IOException.class));
  }

  @Test
  public void batchDoesNotRetryForNonNetworkErrors() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Call<ResponseBody> networkFailure = Calls.failure(new RuntimeException());
    Mockito.when(rudderService.upload(batch)).thenReturn(networkFailure);

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, DEFAULT_RETRIES);
    batchUploadTask.run();

    // Verify we only tried to upload once.
    Mockito.verify(rudderService).upload(batch);
    Mockito.verify(callback).failure(ArgumentMatchers.eq(trackMessage), ArgumentMatchers.any(RuntimeException.class));
  }

  @Test
  public void givesUpAfterMaxRetries() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Mockito.when(rudderService.upload(batch))
        .thenAnswer(
            new Answer<Call<UploadResponse>>() {
              public Call<UploadResponse> answer(InvocationOnMock invocation) {
                Response<UploadResponse> failResponse =
                    Response.error(429, ResponseBody.create(null, "Not Found"));
                return Calls.response(failResponse);
              }
            });

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 10);
    batchUploadTask.run();

    // DEFAULT_RETRIES == maxRetries
    // tries 11(one normal run + 10 retries) even though default is 50 in AnalyticsClient.java
    Mockito.verify(rudderService, Mockito.times(11)).upload(batch);
    Mockito.verify(callback)
        .failure(
            ArgumentMatchers.eq(trackMessage),
            ArgumentMatchers.argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("11 retries exhausted");
                  }
                }));
  }

  @Test
  public void hasDefaultRetriesSetTo3() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Mockito.when(rudderService.upload(batch))
        .thenAnswer(
            new Answer<Call<UploadResponse>>() {
              public Call<UploadResponse> answer(InvocationOnMock invocation) {
                Response<UploadResponse> failResponse =
                    Response.error(429, ResponseBody.create(null, "Not Found"));
                return Calls.response(failResponse);
              }
            });

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 3);
    batchUploadTask.run();

    // DEFAULT_RETRIES == maxRetries
    // tries 11(one normal run + 10 retries)
    Mockito.verify(rudderService, Mockito.times(4)).upload(batch);
    Mockito.verify(callback)
        .failure(
            ArgumentMatchers.eq(trackMessage),
            ArgumentMatchers.argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("4 retries exhausted");
                  }
                }));
  }
  @Test
  public void flushWhenNotShutDown() throws InterruptedException {
    AnalyticsClient client = newClient();

    client.flush();
    Mockito.verify(messageQueue).put(FlushMessage.POISON);
  }

  @Test
  public void flushWhenShutDown() throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.flush();

    Mockito.verify(messageQueue, Mockito.times(0)).put(ArgumentMatchers.any(Message.class));
  }

  @Test
  public void enqueueWithRegularMessageWhenNotShutdown(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();

    final Message message = builder.get().userId("foo").build();
    client.enqueue(message);

    Mockito.verify(messageQueue).put(message);
  }

  @Test
  public void enqueueWithRegularMessageWhenShutdown(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.enqueue(builder.get().userId("foo").build());

    Mockito.verify(messageQueue, Mockito.times(0)).put(ArgumentMatchers.any(Message.class));
  }

  @Test
  public void enqueueWithStopMessageWhenShutdown() throws InterruptedException {
    AnalyticsClient client = newClient();
    isShutDown.set(true);

    client.enqueue(STOP);

    Mockito.verify(messageQueue).put(STOP);
  }

  @Test
  public void shutdownWhenAlreadyShutDown() throws InterruptedException {
    isShutDown.set(true); //shutdown already
    AnalyticsClient client = newClient();
//    isShutDown.set(true);

    client.shutdown();

    Mockito.verify(messageQueue, Mockito.times(0)).put(ArgumentMatchers.any(Message.class));
    Mockito.verifyNoInteractions(networkExecutor, callback, rudderService);
  }

  @Test
  public void shutdownWithNoMessageInTheQueue() throws InterruptedException {
    AnalyticsClient client = newClient();
    client.shutdown();

    Mockito.verify(messageQueue).put(STOP);
    Mockito.verify(networkExecutor).shutdown();
    Mockito.verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
    Mockito.verifyNoMoreInteractions(networkExecutor);
  }

  @Test
  public void shutdownWithMessagesInTheQueue(MessageBuilderTest builder)
      throws InterruptedException {
    AnalyticsClient client = newClient();

    client.enqueue(builder.get().userId("foo").build());
    client.shutdown();

    Mockito.verify(messageQueue).put(STOP);
    Mockito.verify(networkExecutor).shutdown();
    Mockito.verify(networkExecutor).awaitTermination(1, TimeUnit.SECONDS);
    Mockito.verify(networkExecutor).submit(ArgumentMatchers.any(AnalyticsClient.BatchUploadTask.class));
  }

  @Test
  public void neverRetries() {
    AnalyticsClient client = newClient();
    TrackMessage trackMessage = TrackMessage.builder("foo").userId("bar").build();
    Batch batch = batchFor(trackMessage);

    Mockito.when(rudderService.upload(batch))
        .thenAnswer(
            new Answer<Call<UploadResponse>>() {
              public Call<UploadResponse> answer(InvocationOnMock invocation) {
                Response<UploadResponse> failResponse =
                    Response.error(429, ResponseBody.create(null, "Not Found"));
                return Calls.response(failResponse);
              }
            });

    BatchUploadTask batchUploadTask = new BatchUploadTask(client, BACKO, batch, 0);
    batchUploadTask.run();

    // runs once but never retries
    Mockito.verify(rudderService, Mockito.times(1)).upload(batch);
    Mockito.verify(callback)
        .failure(
            ArgumentMatchers.eq(trackMessage),
            ArgumentMatchers.argThat(
                new ArgumentMatcher<IOException>() {
                  @Override
                  public boolean matches(IOException exception) {
                    return exception.getMessage().equals("1 retries exhausted");
                  }
                }));
  }
}
