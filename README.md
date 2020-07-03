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
compile 'com.rudderstack.sdk.java:rudderanalytics:+'
```

## Initialize ```RudderClient```
```java
RudderAnalytics analytics = RudderAnalytics.builder(
        "1dgURTY4fptGJK0c0RA8SXr7l9z",
        "https://3a5931a1e147.ngrok.io"
).build();
```

## Send Events
```java
Map<String, Object> map = new HashMap<>();
map.put("name", "Michael Bolton");
map.put("email", "mbolton@example.com");
analytics.enqueue(IdentifyMessage.builder()
        .userId("f4ca124298")
        .traits(map)
);
```

## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Discord](https://discordapp.com/invite/xNEdEGw) channel. We will be happy to help you.
