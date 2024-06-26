/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
