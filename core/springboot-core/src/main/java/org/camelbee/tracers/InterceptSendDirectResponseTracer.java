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
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for tracing Send Direct Responses via logger beans.
 */
public class InterceptSendDirectResponseTracer extends InterceptorTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(InterceptSendDirectResponseTracer.class);

  @Override
  public void trace(Exchange exchange, MessageService messageService) {

    try {

      String responseBody = getBodyAndConvertInputStreamsToString(exchange);

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

      Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

      String exception = cause != null ? cause.getLocalizedMessage() : null;

      messageService.addMessage(new Message(exchange.getExchangeId(), responseBody, requestHeaders, callerRoute,
          currentRoute, MessageType.RESPONSE, exception));

    } catch (Exception e) {
      LOGGER.error("Could not trace Send Direct Response Exchange: {} with exception: {}", exchange, e);
    }

  }
}
