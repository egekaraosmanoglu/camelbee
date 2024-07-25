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

package org.camelbee.config;

import org.apache.camel.CamelContext;
import org.apache.camel.support.EventNotifierSupport;
import org.camelbee.notifier.CamelBeeEventNotifier;
import org.camelbee.tracers.TracerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CamelBeeEventNotifierConfigurer.
 */
@Configuration
public class CamelBeeEventNotifierConfigurer {

  /**
   * Creates EventNotifierSupport bean.
   *
   * @param camelContext  The camelContext.
   * @param tracerService The tracerService.
   * @return EventNotifierSupport bean.
   */
  @Bean
  public EventNotifierSupport eventNotifier(CamelContext camelContext, TracerService tracerService) {
    final CamelBeeEventNotifier camelBeeEventNotifier = new CamelBeeEventNotifier(tracerService);
    camelContext.getManagementStrategy().addEventNotifier(camelBeeEventNotifier);
    return camelBeeEventNotifier;
  }
}
