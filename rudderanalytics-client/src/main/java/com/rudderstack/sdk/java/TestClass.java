package com.rudderstack.sdk.java;

import com.rudderstack.sdk.java.messages.AliasMessage;
import com.rudderstack.sdk.java.messages.IdentifyMessage;
import java.util.HashMap;
import java.util.Map;

class Enqueue {
     public void startEnqueue(RudderAnalytics analytics) {
        Map<String, Object> map = new HashMap<>();
            map.put("name", "Michael Bolton");
            map.put("email", "mbolton@example.com");
            
            analytics.enqueue(IdentifyMessage.builder()
                .userId("f4ca124298")
                .traits(map)
            );
            analytics.enqueue(AliasMessage.builder("previousId")
                .userId("f4ca124298")
            );
            System.out.println("Pre-Flush");
            // analytics.flush();
            analytics.blockFlush();
            System.out.println("After flush");
    }
}


class ThreadTest implements Runnable{
    Thread t;
    RudderAnalytics init;
    ThreadTest (RudderAnalytics analytics) {
        
        init = analytics;
        t = new Thread(this);
        System.out.println("Thread : " + t);
        t.start();
        
    }
    public void run() {
        // synchronized(this) {
        Enqueue en = new Enqueue();
        en.startEnqueue(init);

        // try {
            
            // Map<String, Object> map = new HashMap<>();
            // map.put("name", "Michael Bolton");
            // map.put("email", "mbolton@example.com");
            // analytics.enqueue(IdentifyMessage.builder()
            //     .userId("f4ca124298")
            //     .traits(map)
            // );
            // analytics.enqueue(AliasMessage.builder("previousId")
            //     .userId("f4ca124298")
            // );
            // System.out.println("Pre-Flush");
            // // analytics.flush();
            // analytics.blockFlush();

            // analytics.enqueue(IdentifyMessage.builder()
            //         .userId("f4ca1242987674fg")
            //         .traits(map)
            // );
            // analytics.enqueue(AliasMessage.builder("previousId")
            //         .userId("f4ca124298hjkhj")
            // );
            // // analytics.flush();
            // analytics.blockFlush();
            System.out.println("Hello World!");
            en.startEnqueue(init);
            System.out.println("Hello World2!");
            en.startEnqueue(init);
            System.out.println("Hello World3!");
        // }
        // }
    //    catch (InterruptedException e) {
    //         System.out.println(name + "Interrupted");
    //    }
            // System.out.println(name + " exiting.");
       }

       public void start () {
        // System.out.println("Starting " +  name );
        if (t == null) {
           t = new Thread (this);
           t.start ();
        }
     }
    }

class RudderInitialize {
    public RudderAnalytics initialize() {
    RudderAnalytics analytics = RudderAnalytics.builder(
        "1YLNtGIKMxFdLGO0PouInv5VTS9",
        "http://localhost:8080"
)
      .log(new Log() {
          @Override
          public void print(Level level, String s, Object... objects) {
              System.out.println(s);
          }

          @Override
          public void print(Level level, Throwable throwable, String s, Object... objects) {
              System.out.println(s);
          }
      })
        // .flushQueueSize(2)
        .synchronize(true)
        .plugin(new PluginLog())
        .build();
        System.out.println("abcd");
        return analytics;
    }
}


public class TestClass {
    public static void main(String[] args) {
       
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
    //         // .flushQueueSize(2)
    //         .synchronize(true)
    //         .plugin(new PluginLog())
    //         .build();
        RudderInitialize  rudder = new RudderInitialize(); 
        RudderAnalytics analytics = rudder.initialize();
        ThreadTest t1 = new ThreadTest(analytics);
        t1.start();
        // ThreadTest t2 = new ThreadTest( analytics);
        //  t2.start();
     }
}


