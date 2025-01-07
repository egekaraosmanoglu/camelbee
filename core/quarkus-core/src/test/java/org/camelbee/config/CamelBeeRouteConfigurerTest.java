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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.debugger.service.RouteContextService;
import org.camelbee.tracers.ExchangeCompletedEventTracer;
import org.camelbee.tracers.ExchangeCreatedEventTracer;
import org.camelbee.tracers.ExchangeSendingEventTracer;
import org.camelbee.tracers.ExchangeSentEventTracer;
import org.camelbee.tracers.TracerService;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CamelBeeRouteConfigurerTest extends CamelQuarkusTestSupport {

  @Inject
  ProducerTemplate producerTemplate;

  @EndpointInject("mock:test")
  MockEndpoint mockEndpoint;

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Inject
  TracerService tracerService;

  @Inject
  MessageService messageService;

  @Inject
  ExchangeCreatedEventTracer exchangeCreatedEventTracer;

  @Inject
  ExchangeSendingEventTracer exchangeSendingEventTracer;

  @Inject
  ExchangeSentEventTracer exchangeSentEventTracer;

  @Inject
  ExchangeCompletedEventTracer exchangeCompletedEventTracer;

  @Inject
  RouteContextService routeContextService;

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        camelBeeRouteConfigurer.configureRoute(this);

        from("direct:test").to("mock:test");
      }
    };
  }

  @Test
  void shouldAutowiredProducerTemplate() {
    assertNotNull(producerTemplate);
  }

  @Test
  void shouldInjectEndpoint() throws InterruptedException {
    mockEndpoint.setExpectedMessageCount(1);
    producerTemplate.sendBody("direct:test", "testMessage");
    mockEndpoint.assertIsSatisfied();
  }
}
