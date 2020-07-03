# What is Rudder?

**Short answer:**
Rudder is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:**
Rudder is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

Released under [MIT License](https://opensource.org/licenses/MIT)

## Getting Started with JAVA SDK

*Add to `pom.xml`:*

```xml
<dependency>
   <groupId>com.rudderstack.sdk.java</groupId>
	 <artifactId>rudderanalytics-client</artifactId>
   <version>1.0.0</version>
</dependency>
```
and
```xml
<repositories>
       <repository>
           <id>bintray</id>
           <name>rudderstack</name>
           <url>https://dl.bintray.com/rudderstack/rudderstack</url>
       </repository>
</repositories>
```

*or if you're using Gradle:*

```bash
implementation 'com.rudderstack.sdk.java:rudderanalytics-client:1.0.0'
```

## Initialize ```RudderClient```
```java
RudderAnalytics analytics = RudderAnalytics.builder(
        "write_key",
        "http://data-plane-url"
).build();
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
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Discord](https://discordapp.com/invite/xNEdEGw) channel. We will be happy to help you.
