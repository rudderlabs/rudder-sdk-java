package com.rudder.analytics.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rudder.analytics.Analytics;

/**
 * Spring Boot autoconfiguration class for Rudder Analytics.
 *
 * @author Christopher Smith
 */
@Configuration
@EnableConfigurationProperties(RudderProperties.class)
@ConditionalOnProperty("rudder.analytics.writeKey")
public class RudderAnalyticsAutoConfiguration {

  @Autowired private RudderProperties properties;

  @Bean
  public Analytics rudderAnalytics() {
    return Analytics.builder(properties.getWriteKey()," ").build();
  }
}
