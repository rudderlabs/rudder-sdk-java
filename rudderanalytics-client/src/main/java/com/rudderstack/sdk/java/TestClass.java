package com.rudderstack.sdk.java;

// import com.rudderstack.sdk.java.messages.AliasMessage;
// import com.rudderstack.sdk.java.messages.IdentifyMessage;
// import java.util.HashMap;
// import java.util.Map;

// class Enqueue {
//      public void startEnqueue(RudderAnalytics analytics) {
//         Map<String, Object> map = new HashMap<>();
//             map.put("name", "Michael Bolton");
//             map.put("email", "mbolton@example.com");
            
//             analytics.enqueue(IdentifyMessage.builder()
//                 .userId("Flush1Identify" + Thread.currentThread().getName())
//                 .traits(map)
//             );
//             analytics.enqueue(AliasMessage.builder("previousId")
//                 .userId("Flush1Alias" + Thread.currentThread().getName())
//             );
//             analytics.blockFlush();
//             analytics.enqueue(IdentifyMessage.builder()
//                     .userId("Flush2Identify" + Thread.currentThread().getName())
//                     .traits(map)
//             );
//             analytics.enqueue(AliasMessage.builder("previousId")
//                     .userId("Flush2Alias" + Thread.currentThread().getName())
//             );
//             analytics.blockFlush();
//     }
// }

// class ThreadTest implements Runnable{
//     Thread t;
//     RudderAnalytics init;
//     ThreadTest (RudderAnalytics analytics) { 
//         init = analytics;
//         t = new Thread(this);
//         t.start();   
//     }
//     public void run() {
//         Enqueue en = new Enqueue();
//         en.startEnqueue(init);
//        }
//        public void start () {
//         if (t == null) {
//            t = new Thread (this);
//            t.start ();
//         }
//      }
//     }
// class RudderInitialize {
//     public RudderAnalytics initialize() {
//     RudderAnalytics analytics = RudderAnalytics.builder(
//         "1YLNtGIKMxFdLGO0PouInv5VTS9",
//         "http://localhost:8080"
// )
//       .log(new Log() {
//           @Override
//           public void print(Level level, String s, Object... objects) {
//               System.out.println(s);
//           }

//           @Override
//           public void print(Level level, Throwable throwable, String s, Object... objects) {
//               System.out.println(s);
//           }
//       })
//         .synchronize(true)
//         .plugin(new PluginLog())
//         .build();
//         return analytics;
//     }
// }

public class TestClass {
    // public static void main(String[] args) {
    //     RudderInitialize  rudder = new RudderInitialize(); 
    //     RudderAnalytics analytics = rudder.initialize();
    //     ThreadTest t1 = new ThreadTest(analytics);
    //     t1.start();
    //     ThreadTest t2 = new ThreadTest( analytics);
    //     t2.start();
    //  }
}


