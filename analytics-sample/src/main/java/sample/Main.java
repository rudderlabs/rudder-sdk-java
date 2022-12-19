package sample;

import com.rudderstack.sdk.java.analytics.RudderAnalytics;
import com.rudderstack.sdk.java.analytics.messages.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.OkHttpClient;

public class Main {
  public static void main(String... args) throws Exception {
    final BlockingFlush blockingFlush = BlockingFlush.create();
    // in case there's a chance of registered parties count going beyond
    // 65563, opt for TierBlocking flush instead.

//    final TierBlockingFlush blockingFlush = TierBlockingFlush.create();

    // https://rudder.com/rudder-engineering/sources/test-java/debugger
    final RudderAnalytics analytics =
            RudderAnalytics.builder("write_key")
                    .setDataPlaneUrl("data_plane_url")
                    .plugin(blockingFlush.plugin())
                    .plugin(new LoggingPlugin())
                    .client(createClient())
                    .build();

    final String userId = System.getProperty("user.name");
    final String anonymousId = UUID.randomUUID().toString();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("count", count.incrementAndGet());
        analytics.enqueue(
                TrackMessage.builder("Java Test")
                        .properties(properties)
                        .anonymousId(anonymousId)
                        .userId(userId));
      }
    }

    analytics.flush();
    blockingFlush.block();
    System.out.println("Blocking flush complete");
    analytics.shutdown();
  }


  /**
   * By default, the analytics client uses an HTTP client with sane defaults. However you can
   * customize the client to your needs. For instance, this client is configured to automatically
   * gzip outgoing requests.
   */
  private static OkHttpClient createClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
//        .addInterceptor(new GzipRequestInterceptor())
            .build();
  }
}