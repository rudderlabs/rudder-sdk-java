package com.rudderstack.sdk.java;

import com.rudderstack.sdk.java.messages.AliasMessage;
import com.rudderstack.sdk.java.messages.IdentifyMessage;

import java.util.HashMap;
import java.util.Map;

public class TestClass {
     public static void main(String[] args) {
        //  RudderAnalytics analytics = RudderAnalytics.builder(
        //          "YOUR_WRITE_KEY",
        //          "DATA_PLANE_URL"
        //  )
        //        .log(new Log() {
        //            @Override
        //            public void print(Level level, String s, Object... objects) {
        //                System.out.println(s);
        //            }

        //            @Override
        //            public void print(Level level, Throwable throwable, String s, Object... objects) {
        //                System.out.println(s);
        //            }
        //        })
        //          .synchronize(true)
        //          .plugin(new PluginLog())
        //          .build();
        //  Map<String, Object> map = new HashMap<>();
        //  map.put("name", "Michael Bolton");
        //  map.put("email", "mbolton@example.com");
        //  analytics.enqueue(IdentifyMessage.builder()
        //          .userId("f4ca124298")
        //          .traits(map)
        //  );
        //  analytics.enqueue(AliasMessage.builder("previousId")
        //          .userId("f4ca124298")
        //  );
        //  System.out.println("Pre-Flush");
        //  analytics.flush();
        //  analytics.blockFlush();
        //  System.out.println("Hello World!");
     }
}
