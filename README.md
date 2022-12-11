[![Build & Code Quality Checks](https://github.com/rudderlabs/rudder-sdk-java/actions/workflows/build-and-quality-checks.yml/badge.svg?branch=ci%2FaddCIFeatures)](https://github.com/rudderlabs/rudder-sdk-java/actions/workflows/build-and-quality-checks.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

## RudderStack's Java SDK

RudderStack’s Java SDK allows you to track your customer event data from your Java code. Once enabled, the event requests hit the RudderStack servers. RudderStack then routes the events to the specified destination platforms as configured by you.

For detailed documentation on the Java SDK, click [here](https://www.rudderstack.com/docs/sources/event-streams/sdks/rudderstack-java-sdk/).

## Getting Started with the RudderStack Java SDK

*Add to `pom.xml`:*

```xml
<dependency>
    <groupId>com.rudderstack.sdk.java.analytics</groupId>
    <artifactId>analytics</artifactId>
    <version>2.1.0</version>
</dependency>

```

*or if you're using Gradle:*

```bash
implementation 'com.rudderstack.sdk.java.analytics:analytics:2.1.0'
```

## Initializing ```RudderClient```

```java 
RudderAnalytics analytics = RudderAnalytics
         .builder("<WRITE_KEY>")
         .endpoint("<DATA_PLANE_URL>")
         .build();
```

## Sending events

```java
Map<String, Object> map = new HashMap<>();
map.put("name", "John Marshal");
map.put("email", "john@example.com");
analytics.enqueue(IdentifyMessage.builder()
        .userId("6754ds7d9")
        .traits(map)
);
```

For more information on the different types of events supported by the Java SDK, refer to our [docs](https://www.rudderstack.com/docs/sources/event-streams/sdks/rudderstack-java-sdk/).

## Contact Us

If you come across any issues while configuring or using this SDK, feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
