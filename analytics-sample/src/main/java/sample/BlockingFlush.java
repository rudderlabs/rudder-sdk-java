package sample;

import com.rudderstack.sdk.java.analytics.RudderAnalytics;
import com.rudderstack.sdk.java.analytics.Callback;
import com.rudderstack.sdk.java.analytics.MessageTransformer;
import com.rudderstack.sdk.java.analytics.Plugin;
import com.rudderstack.sdk.java.analytics.messages.Message;
import com.rudderstack.sdk.java.analytics.messages.MessageBuilder;
import java.util.concurrent.Phaser;

/**
 * The {@link RudderAnalytics} class doesn't come with a blocking {@link RudderAnalytics#flush()} implementation
 * out of the box. It's trivial to build one using a {@link Phaser} that monitors requests and is
 * able to block until they're uploaded.
 *
 * <pre><code>
 * BlockingFlush blockingFlush = BlockingFlush.create();
 * RudderAnalytics analytics = RudderAnalytics.builder(writeKey)
 *      .plugin(blockingFlush.plugin())
 *      .build();
 *
 * // Do some work.
 *
 * analytics.flush(); // Trigger a flush.
 * blockingFlush.block(); // Block until the flush completes.
 * analytics.shutdown(); // Shut down after the flush is complete.
 * </code></pre>
 */
public class BlockingFlush {

  public static BlockingFlush create() {
    return new BlockingFlush();
  }

  BlockingFlush() {
    this.phaser = new Phaser(1);
  }

  final Phaser phaser;

  public Plugin plugin() {
    return new Plugin() {
      @Override
      public void configure(RudderAnalytics.Builder builder) {
        builder.messageTransformer(
            new MessageTransformer() {
              @Override
              public boolean transform(MessageBuilder builder) {
                phaser.register();
                return true;
              }
            });

        builder.callback(
            new Callback() {
              @Override
              public void success(Message message) {
                phaser.arrive();
              }

              @Override
              public void failure(Message message, Throwable throwable) {
                phaser.arrive();
              }
            });
      }
    };
  }

  public void block() {
    phaser.arriveAndAwaitAdvance();
  }
}
