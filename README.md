
# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## RudderStack's Java SDK

RudderStack’s Java SDK allows you to track your customer event data from your Java code. Once enabled, the event requests hit the RudderStack servers. RudderStack then routes the events to the specified destination platforms as configured by you.

## Getting Started with the RudderStack Java SDK

*Add to `pom.xml`:*

```xml
<dependency>
    <groupId>com.rudderstack.sdk.java.analytics</groupId>
    <artifactId>analytics</artifactId>
    <version>2.0.1</version>
</dependency>

```

*or if you're using Gradle:*

```bash
implementation 'com.rudderstack.sdk.java.analytics:analytics:2.0.1'
```

## Initialize ```RudderClient```

```java 
RudderAnalytics analytics = RudderAnalytics.builder(
        "write_key",
        "http://data-plane-url"
)
.synchronize(true) // optional (default : false).It is required to block further method invocation until the flush completes.
.plugin(new PluginLog()) // optional. Used for Logging 
.build();

...YOUR CODE...

analytics.flush(); // Triggers a flush.
analytics.blockFlush(); //optional. Triggers a flush and block until the flush completes. Required in case of Synchronize. It calls implicitly the `flush` method. So, explicit `flush` call is not required.
analytics.shutdown(); // Shut down after the flush is complete.
```

## Send Events

```java
Map<String, Object> map = new HashMap<>();
map.put("name", "John Marshal");
map.put("email", "john@example.com");
analytics.enqueue(IdentifyMessage.builder()
        .userId("6754ds7d9")
        .traits(map)
);
```

## Contact Us

If you come across any issues while configuring or using this SDK, feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
