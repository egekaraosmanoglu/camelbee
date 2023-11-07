package io.camelbee.springboot.example.config;

import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures CamelExchanges Metric Registry for Kubernetes/Openshift Monitoring.
 */
@Configuration
public class MonitoringConfig {

  /**
   * MicrometerRoutePolicyFactory bean.
   */
  @Bean
  public MicrometerRoutePolicyFactory micrometerRoutePolicyFactory() {
    return new MicrometerRoutePolicyFactory();
  }
}
