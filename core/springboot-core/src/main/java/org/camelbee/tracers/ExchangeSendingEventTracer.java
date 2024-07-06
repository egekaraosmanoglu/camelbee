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

package org.camelbee.tracers;

import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.ExchangeSendingEvent;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.ExtendedExchangeExtension;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for tracing Send Direct Requests triggered by the endpoint interceptor configured in the route configuration {@see
 * CamelBeeRouteConfigurer.configure}.
 */
public class ExchangeSendingEventTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeSendingEventTracer.class);

  public static Message traceEvent(ExchangeSendingEvent event) {

    Exchange exchange = event.getExchange();
    String endpointUri = event.getEndpoint().getEndpointUri();
    /*
    if an error happened before and still in failed state
    remove that state since we are calling a new endpoint
    */
    exchange.removeProperty(CamelBeeConstants.CAMEL_FAILED_EVENT_STATE);

    try {

      String requestBody = null;

      if (exchange.getIn().getBody() != null) {

        requestBody = exchange.getIn().getBody(String.class);

      }

      Deque<String> stack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);

      // happens only if a route is called via the producertemplate.
      if (stack == null) {
        stack = new ArrayDeque<>();
      }

      Deque<String> clonedStack = new ArrayDeque<>(stack);

      clonedStack.peek();

      final String currentRouteProperty = (String) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME);

      final String currentRoute = endpointUri; //(String) exchange.getProperty(Exchange.TO_ENDPOINT);

      final String endpointId = ((ExtendedExchangeExtension) ((DefaultExchange) exchange).getExchangeExtension()).getHistoryNodeId();

      clonedStack.push(currentRoute);

      exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedStack);

      exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, currentRoute);

      final var requestHeaders = ExchangeUtils.getHeaders(exchange);

      Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

      String exception = cause != null ? cause.getLocalizedMessage() : null;

      return new Message(exchange.getExchangeId(), requestBody, requestHeaders, currentRouteProperty,
          currentRoute, endpointId, MessageType.REQUEST, exception);

    } catch (Exception e) {
      LOGGER.error("Could not trace Send Direct Request Exchange: {} with exception: {}", exchange, e);
    }

    return null;

  }

}
