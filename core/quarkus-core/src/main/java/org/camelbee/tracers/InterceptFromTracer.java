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
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.cxf.message.MessageContentsList;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for tracing Direct From Interceptor requests.
 */
public class InterceptFromTracer extends InterceptorTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(InterceptFromTracer.class);

  @Override
  public void trace(Exchange exchange, MessageService messageService) {

    //  trace only first From endpoint
    if (exchange.getProperty(CamelBeeConstants.INITIAL_MESSAGE) != null) {
      return;
    }
    exchange.setProperty(CamelBeeConstants.INITIAL_MESSAGE, "received");

    try {

      final String directRequestBody;

      if (exchange.getIn().getBody() instanceof InputStreamCache requestStream) {

        directRequestBody = new String(requestStream.readAllBytes());

      } else if (exchange.getIn().getBody() instanceof MessageContentsList msgList) {

        directRequestBody = !msgList.isEmpty() ? msgList.get(0).toString() : null;

      } else if (exchange.getIn().getBody() != null) {

        directRequestBody = exchange.getIn().getBody(String.class);

      } else {
        directRequestBody = null;
      }

      final var requestHeaders = ExchangeUtils.getHeaders(exchange);

      addMessage(exchange, messageService, directRequestBody, requestHeaders);

    } catch (Exception e) {
      LOGGER.error("Could not trace From Direct Request Exchange: {} with exception: {}", exchange, e);
    }

  }

  private void addMessage(Exchange exchange, MessageService messageService, String directRequestBody, String requestHeaders) {

    final String currentRouteName = (String) exchange.getProperty(Exchange.TO_ENDPOINT);

    final String callerRoute = exchange.getFromRouteId();

    Deque<String> stack = new ArrayDeque<>();

    stack.push(callerRoute);

    exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, stack);

    exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, currentRouteName != null ? currentRouteName : callerRoute);

    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

    String exception = cause != null ? cause.getLocalizedMessage() : null;

    messageService.addMessage(new Message(exchange.getExchangeId(), directRequestBody, requestHeaders, callerRoute,
        currentRouteName, MessageType.REQUEST, exception));

  }

}
