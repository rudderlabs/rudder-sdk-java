package sample;

import com.rudderstack.sdk.Analytics;
import com.rudderstack.sdk.Callback;
import com.rudderstack.sdk.Log;
import com.rudderstack.sdk.Plugin;
import com.rudderstack.sdk.messages.Message;

/**
 * A {@link Plugin} implementation that redirects client logs to standard output and logs callback
 * events.
 */
public class LoggingPlugin implements Plugin {
  @Override
  public void configure(Analytics.Builder builder) {
    builder.log(
        new Log() {
          @Override
          public void print(Level level, String format, Object... args) {
            System.out.println(level + ":\t" + String.format(format, args));
          }

          @Override
          public void print(Level level, Throwable error, String format, Object... args) {
            System.out.println(level + ":\t" + String.format(format, args));
            System.out.println(error);
          }
        });

    builder.callback(
        new Callback() {
          @Override
          public void success(Message message) {
            System.out.println("Uploaded " + message);
          }

          @Override
          public void failure(Message message, Throwable throwable) {
            System.out.println("Could not upload " + message);
            System.out.println(throwable);
          }
        });
  }
}
