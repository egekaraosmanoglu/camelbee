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
import org.apache.camel.spi.CamelEvent.ExchangeSentEvent;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.ExtendedExchangeExtension;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageEventType;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.utils.ExchangeUtils;
import org.camelbee.utils.TracerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for tracing Send To Responses via logger beans.
 */
@Component
public class ExchangeSentEventTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeSentEventTracer.class);

  private final MessageService messageService;

  public ExchangeSentEventTracer(MessageService messageService) {
    this.messageService = messageService;
  }

  /**
   * Trace ExchangeSentEvent.
   *
   * @param event The ExchangeSentEvent.
   * @return The Messages.
   */
  public void traceEvent(ExchangeSentEvent event) {

    Exchange exchange = event.getExchange();

    try {

      /*
        endpoint called from ProducerController is also intercepted here
        which we should not put into the messages
      */
      if (exchange.getProperty(CamelBeeConstants.CAMEL_PRODUCED_EXCHANGE) != null) {
        return;
      }

      Deque<String> routeStack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);
      Deque<String> clonedRouteStack = new ArrayDeque<>(routeStack);

      final String currentRoute = clonedRouteStack.pop();
      final String callerRoute = clonedRouteStack.peek();

      exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedRouteStack);

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

      String responseCompletedBody = ""; // ExchangeUtils.readBodyAsString(exchange);

      /*
        if this ExchangeSentEvent is triggered after another ExchangeSentEvent
        then endpointId will be null, Camel does not keep track of nested getHistoryNodeId
        that's why we need the stack
       */
      final String endpointId = ((ExtendedExchangeExtension) ((DefaultExchange) exchange).getExchangeExtension()).getHistoryNodeId();

      messageService.addMessage(new Message(exchange.getExchangeId(), MessageEventType.SENT, responseCompletedBody, requestHeaders, callerRoute,
          currentRoute, endpointId, messageType, errorMessage));

    } catch (Exception e) {
      LOGGER.error("Could not trace Send To Response Exchange: {} with exception: {}", exchange, e);
    }

  }
}
