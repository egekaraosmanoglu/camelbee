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

import org.apache.camel.Exchange;
import org.apache.camel.impl.event.ExchangeSentEvent;
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
 * Responsible for tracing Send To Responses via logger beans.
 */
public class ExchangeSentEventTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeSentEventTracer.class);

  public static Message traceEvent(ExchangeSentEvent event) {

    Exchange exchange = event.getExchange();
    String endpointUri = event.getEndpoint().getEndpointUri();

    try {

      /*
        endpoint called from ProducerController is also intercepted here
        which we should not put into the messages
      */
      if (exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME) == null) {
        return null;
      }

      String responseBody = ExchangeUtils.getBodyAndConvertInputStreamsToString(exchange);

      final var requestHeaders = ExchangeUtils.getHeaders(exchange);

      MessageType messageType = MessageType.RESPONSE;

      String errorMessage = TracerUtils.handleError(exchange);

      if (errorMessage != null) {
        messageType = MessageType.ERROR_RESPONSE;
      }

      final String endpointId = ((ExtendedExchangeExtension) ((DefaultExchange) exchange).getExchangeExtension()).getHistoryNodeId();

      return new Message(exchange.getExchangeId(), responseBody, requestHeaders, (String) exchange.getProperty(
          CamelBeeConstants.CURRENT_ROUTE_NAME),
          (String) exchange.getProperty(CamelBeeConstants.SEND_ENDPOINT), endpointId, messageType, errorMessage);

    } catch (Exception e) {
      LOGGER.error("Could not trace Send To Response Exchange: {} with exception: {}", exchange, e);
    }

    return null;

  }
}
