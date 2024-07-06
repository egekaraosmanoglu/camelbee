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
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.ExtendedExchangeExtension;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.utils.ExchangeUtils;
import org.camelbee.utils.TracerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for tracing Send Direct Responses via logger beans.
 */
public class ExchangeCompletedEventTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeCompletedEventTracer.class);

  public static Message traceEvent(ExchangeCompletedEvent event) {

    Exchange exchange = event.getExchange();
    try {

      Deque<String> stack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);
      Deque<String> clonedStack = new ArrayDeque<>(stack);

      final String currentRoute = clonedStack.pop();
      final String callerRoute = clonedStack.peek();

      exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedStack);

      /*
       set the previous route (callerRoute) as the current route
       which would be used in SendToRequestTracers
       */
      exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, callerRoute);

      final var requestHeaders = ExchangeUtils.getHeaders(exchange);

      MessageType messageType = MessageType.RESPONSE;

      String errorMessage = TracerUtils.handleError(exchange);

      if (errorMessage != null) {
        messageType = MessageType.ERROR_RESPONSE;
      }
      final String responseBody = ExchangeUtils.getBodyAndConvertInputStreamsToString(exchange);

      final String endpointId = ((ExtendedExchangeExtension) ((DefaultExchange) exchange).getExchangeExtension()).getHistoryNodeId();

      return new Message(exchange.getExchangeId(), responseBody, requestHeaders, callerRoute,
          currentRoute, endpointId, messageType, errorMessage);

    } catch (Exception e) {
      LOGGER.error("Could not trace Send Direct Response Exchange: {} with exception: {}", exchange, e);
    }

    return null;

  }


}
