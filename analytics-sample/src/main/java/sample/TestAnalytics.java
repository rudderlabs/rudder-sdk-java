package sample;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.rudderstack.sdk.Analytics;
import com.rudderstack.sdk.messages.*;

public class TestAnalytics {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    final Analytics analytics = Analytics.builder("1dgURTY4fptGJK0c0RA8SXr7l9z", "https://85b2ad0864ad.ngrok.io")
        .build();

    final String userId = System.getProperty("user.name");
    final String anonymousId = UUID.randomUUID().toString();

    final AtomicInteger count = new AtomicInteger();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("count", count.incrementAndGet());
        analytics
            .enqueue(TrackMessage.builder("Java Test").properties(properties).anonymousId(anonymousId).userId(userId));
      }
    }
    Map<String, String> traits = new HashMap<>();
    traits.put("firstName", "firstname");
    traits.put("name", "some name");
    System.out.println("Loadded and tracked");
    analytics.enqueue(IdentifyMessage.builder().userId("f4ca124298").traits(traits));

    analytics.enqueue(ScreenMessage.builder("Schedule").userId("f4ca124298"));

    analytics.enqueue(PageMessage.builder("Schedule").userId("f4ca124298"));

    analytics.enqueue(GroupMessage.builder("some-group-id").userId("f4ca124298"));

    analytics.enqueue(AliasMessage.builder("previousId").userId("f4ca124298"));
  }
}
