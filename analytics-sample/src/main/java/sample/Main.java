package sample;

import com.rudderstack.sdk.java.analytics.RudderAnalytics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.rudderstack.sdk.java.analytics.messages.*;
import okhttp3.OkHttpClient;

public class Main {
  public static void main(String... args) throws Exception {
    final BlockingFlush blockingFlush = BlockingFlush.create();

    // https://rudder.com/rudder-engineering/sources/test-java/debugger
    final RudderAnalytics analytics =
            RudderAnalytics.builder("21x5I7SYAW3JWmUEnuQar1pfbs1")
                    .endpoint("https://863c-14-97-100-194.ngrok.io")
                    .plugin(blockingFlush.plugin())
                    .plugin(new LoggingPlugin())
                    .client(createClient())
                    .build();

    final String userId = System.getProperty("user.name");
    final String anonymousId = UUID.randomUUID().toString();

    final AtomicInteger count = new AtomicInteger();
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("key1", "value1");
    properties.put("key2", "value2");
    properties.put("count", count.incrementAndGet());
    analytics.enqueue(
            TrackMessage.builder("Java Test")
                    .properties(properties)
                    .anonymousId(anonymousId)
                    .userId(userId));
    //  }
    //}


    analytics.enqueue(
            TrackMessage.builder("Java Test")
                    .properties(properties)
                    .anonymousId(anonymousId)
                    .userId(userId)
    );
    Map<String, Object> traits = new LinkedHashMap<>();
    traits.put("name", "Sample Name");
    traits.put("email", "sample@abc.com");

    analytics.enqueue(IdentifyMessage.builder()
            .userId("f4ca124298")
            .traits(traits)
    );
    analytics.enqueue(PageMessage.builder("Schedule")
            .userId("abcfgrg")
            .properties(properties)
    );

    analytics.enqueue(ScreenMessage.builder("Schedule")
            .userId("f4ca124298")
            .properties(properties)
    );

    analytics.enqueue(GroupMessage.builder("group123")
            .userId("f4ca124298")
            .traits(properties
            )
    );

    analytics.enqueue(AliasMessage.builder("previousId")
            .userId("newId")
    );

    analytics.enqueue(
            TrackMessage.builder("Java Test 2")
                    .properties(properties)
                    .anonymousId(anonymousId)
                    .userId(userId)
    );


    analytics.flush();
    blockingFlush.block();
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