package com.rudderstack.sdk.java;

import com.rudderstack.sdk.java.messages.AliasMessage;
import com.rudderstack.sdk.java.messages.IdentifyMessage;
import java.util.HashMap;
import java.util.Map;


class ThreadTest implements Runnable{
    Thread t;
    String name;
    ThreadTest (String thread) {
        name = thread;
        t = new Thread(this);
        System.out.println("Thread : " + t);
        t.start();
    }
    public void run() {
        // try {
            RudderAnalytics analytics = RudderAnalytics.builder(
                "YOUR_WRITE_KEY",
                "DATA_PLANE_URL"
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
                .synchronize(true)
                .plugin(new PluginLog())
                .build();
                
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
            analytics.flush();
            analytics.blockFlush();

            analytics.enqueue(IdentifyMessage.builder()
                    .userId("f4ca124298")
                    .traits(map)
            );
            analytics.enqueue(AliasMessage.builder("previousId")
                    .userId("f4ca124298")
            );
            analytics.flush();
            analytics.blockFlush();
            System.out.println("Hello World!");
        // }
    //    catch (InterruptedException e) {
    //         System.out.println(name + "Interrupted");
    //    }
            System.out.println(name + " exiting.");
       }

       public void start () {
        System.out.println("Starting " +  name );
        if (t == null) {
           t = new Thread (this, name);
           t.start ();
        }
     }
    }




public class TestClass  {
    public static void main(String[] args) {
        ThreadTest t1 = new ThreadTest("Thread-1");
        t1.start();
        ThreadTest t2 = new ThreadTest("Thread-2");
         t2.start();
     }
}
