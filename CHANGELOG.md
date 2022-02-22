# Version 2.0.0 (December 8, 2021)

- [Fix] Set the default retries to 3
- [Chore] Dependency upgrades


- [Fix] Fix issues around backpressure calculation

- [Fix] maintaining size state of queue

- [New] defaults queue max size to be over 500kb

**Breaking Changes:**

- RudderService now has no url path. If you're using it directly we recommend using setUploadURL

**Pull Requests Merged:**

- [New](https://github.com/rudderio/analytics-java/pull/192) Allow processing of already buffered messages on shutdown
- [New](https://github.com/rudderio/analytics-java/pull/190) Configurable message queue size
- [New](https://github.com/rudderio/analytics-java/pull/189) Configurable retry count
- [New](https://github.com/rudderio/analytics-java/pull/183) Add functionality to set custom endpoint with host and prefix
- [New](https://github.com/rudderio/analytics-java/pull/178) Limit by bytes

- [Fix](https://github.com/rudderio/analytics-java/pull/223) cli wasnt setting event fields
- [Fix](https://github.com/rudderio/analytics-java/pull/222) e2e fix - traits was defined twice in command line opts
- [Fix](https://github.com/rudderio/analytics-java/pull/221) Require either userId or anonymousId \(aligns with other Rudder SDK conventions\)

- [Fix](https://github.com/rudderio/analytics-java/pull/117): This fix gracefully retries temporary HTTP errors such as 5xx server errors. Previously such HTTP errors were not being retried.

- [New](https://github.com/rudderio/analytics-java/pull/113): Allow setting `String` message and anonymous IDs. Previously only UUIDs were accepted. This is a breaking API change and might require you to update your code if you were accessing the `messageId` or `anonymousId` in a transformer or interceptor.

- [New](https://github.com/rudderio/analytics-java/pull/109): Set a custom user-agent for HTTP requests. The default user agent is "analytics-java/version". This user agent is also customizable and can be override for special cases.

```java
final RudderAnalytics analytics = RudderAnalytics.builder(writeKey) //
        .userAgent("custom user agent")
        .build();
```

- [Fix](https://github.com/rudderio/analytics-java/pull/112): Previously the version was being sent as "analytics/version" instead of simply "version".

- [New](https://github.com/rudderio/analytics-java/pull/99): Make endpoint configurable.
- [New](https://github.com/rudderio/analytics-java/pull/101): Allow setting a custom message ID.
- [New](https://github.com/rudderio/analytics-java/pull/58): Allow setting a custom timestamp.

- Fix: Previously, logging Retrofit messages could cause errors if the message contained formatting directives.

- New: Add ability to set multiple Callback instances.
- New: Add plugin API.

```java
class LoggingPlugin implements Plugin {
  @Override public void configure(RudderAnalytics.Builder builder) {
    builder.log(new Log() {
      @Override public void print(Level level, String format, Object... args) {
        System.out.println(level + ":\t" + String.format(format, args));
      }

      @Override public void print(Level level, Throwable error, String format, Object... args) {
        System.out.println(level + ":\t" + String.format(format, args));
        System.out.println(error);
      }
    });

    builder.callback(new Callback() {
      @Override public void success(Message message) {
        System.out.println("Uploaded " + message);
      }

      @Override public void failure(Message message, Throwable throwable) {
        System.out.println("Could not upload " + message);
        System.out.println(throwable);
      }
    });
  }
}

final RudderAnalytics analytics = RudderAnalytics.builder(writeKey) //
        .plugin(new LoggingPlugin())
        .build();
```

- [Fix]: Correctly format and parse dates as per ISO 8601.

- [New]: Add Page API.

- [Fix]: Force ISO 8601 format for dates.
- Use a single thread by default to upload events in the background. Clients can still set their own executor to override this behaviour.
- [New]: Add Callback API.
- [Fix]: Backpressure behaviour. Enqueuing events on a full queue will block instead of throwing an exception.
- Removed Guava dependency.
- [Internal]: Rename enums with lowercase.
