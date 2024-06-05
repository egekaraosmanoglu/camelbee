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

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.tracers.TracerService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The route configurer which sets all listeners, interceptors and the MDCUnitOfWork.
 */
@ApplicationScoped
public class CamelBeeRouteConfigurer {

  @ConfigProperty(name = "camelbee.context-enabled", defaultValue = "false")
  private boolean contextEnabled;

  @ConfigProperty(name = "camelbee.debugger-enabled", defaultValue = "false")
  private boolean debuggerEnabled;

  /**
   * Configures a route for a CamelBee enabled Camel application.
   *
   * @param routeBuilder The routebuilder to be configured.
   */
  public void configureRoute(RouteBuilder routeBuilder) {

    // do not intercept and cache the messages
    if (!contextEnabled || !debuggerEnabled) {
      return;
    }

    routeBuilder.getContext().setStreamCaching(true);
    routeBuilder.getContext().setUseMDCLogging(true);

    // add interceptor for from direct
    routeBuilder.interceptFrom("*").bean(TracerService.CAMELBEE_TRACER,
        TracerService.TRACE_INTERCEPT_FROM_REQUEST).afterPropertiesSet();

    // add interceptor for components other than direct|seda|servlet
    routeBuilder.interceptSendToEndpoint("(?!direct|seda|platform-http).+").bean(TracerService.CAMELBEE_TRACER,
        TracerService.TRACE_INTERCEPT_SEND_TO_REQUEST)
        .afterUri("bean:%s?method=%s".formatted(TracerService.CAMELBEE_TRACER,
            TracerService.TRACE_INTERCEPT_SEND_TO_RESPONSE));

    // add interceptor for direct and seda components
    routeBuilder.interceptSendToEndpoint("^(direct|seda).*").bean(TracerService.CAMELBEE_TRACER,
        TracerService.TRACE_INTERCEPT_SEND_DIRECT_REQUEST)
        .afterUri("bean:%s?method=%s".formatted(TracerService.CAMELBEE_TRACER,
            TracerService.TRACE_INTERCEPT_SEND_DIRECT_RESPONSE));

  }

}
