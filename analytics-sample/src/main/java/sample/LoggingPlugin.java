package sample;

import com.rudder.analytics.Analytics;
import com.rudder.analytics.Callback;
import com.rudder.analytics.Log;
import com.rudder.analytics.Plugin;
import com.rudder.analytics.messages.Message;

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
