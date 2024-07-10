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
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.spi.CamelEvent.ExchangeSendingEvent;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.ExtendedExchangeExtension;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageEventType;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.model.route.CamelRoute;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.debugger.service.RouteContextService;
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for tracing Send Direct Requests triggered by the endpoint interceptor configured in the route configuration {@see
 * CamelBeeRouteConfigurer.configure}.
 */
@Component
public class ExchangeSendingEventTracer {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeSendingEventTracer.class);

  private final MessageService messageService;

  private final RouteContextService routeContextService;

  public ExchangeSendingEventTracer(MessageService messageService, RouteContextService routeContextService) {
    this.messageService = messageService;
    this.routeContextService = routeContextService;
  }

  /**
   * Trace ExchangeSendingEvent.
   *
   * @param event The ExchangeSendingEvent.
   * @return The Messages.
   */
  public void traceEvent(ExchangeSendingEvent event) {

    Exchange exchange = event.getExchange();

    /*
     endpoint called from ProducerController is also intercepted here
     which we should not put into the messages
     */
    if (exchange.getProperty(CamelBeeConstants.CAMEL_PRODUCED_EXCHANGE) != null) {
      return;
    }

    String endpointUri = event.getEndpoint().getEndpointUri();

    try {

      String requestBody = ExchangeUtils.readBodyAsString(exchange);

      addMessage(exchange, endpointUri, requestBody);

    } catch (Exception e) {
      LOGGER.error("Could not trace ExchangeSendingEvent Exchange: {} with exception: {}", exchange, e);
    }

  }

  private void addMessage(Exchange exchange, String endpointUri, String requestBody) {

    final String endpointId = ((ExtendedExchangeExtension) ((DefaultExchange) exchange).getExchangeExtension()).getHistoryNodeId();

    String currentRoute = endpointUri;

    Deque<String> routeStack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);

    /*
     if null then in the initial ExchangeCreatedEvent the exchange.getFromRouteId() was null
     which happens with the platform-http producer component
     */
    if (routeStack == null) {
      routeStack = initializeRouteStack(exchange, endpointId);
    }

    Deque<String> clonedRouteStack = new ArrayDeque<>(routeStack);

    final String currentRouteProperty = (String) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME);

    clonedRouteStack.push(currentRoute);

    exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedRouteStack);

    exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, currentRoute);

    final var requestHeaders = ExchangeUtils.getHeaders(exchange);

    messageService.addMessage(new Message(exchange.getExchangeId(), MessageEventType.SENDING, requestBody, requestHeaders, currentRouteProperty,
        currentRoute, endpointId, MessageType.REQUEST, null));
  }

  private Deque<String> initializeRouteStack(Exchange exchange, String endpointId) {
    //find the actual consumer routeId
    final String actualCurrentRoute = getCallerRouteIdFromRouteContext(endpointId);

    Deque<String> routeStack;
    routeStack = new ArrayDeque<>();
    routeStack.push(actualCurrentRoute);

    /*
     set CURRENT_ROUTE_NAME property which was not set in the ExchangeCreatedEvent
     */
    exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, actualCurrentRoute);

    /*
     for only one time fix the empty routeId of the previous message created in the ExchangeCreatedEvent
     */
    messageService.getMessageList().stream().filter(p -> p.getExchangeId().equals(exchange.getExchangeId())).forEach(q -> q.setRouteId(actualCurrentRoute));

    return routeStack;
  }

  private String getCallerRouteIdFromRouteContext(String endpointId) {

    List<CamelRoute> routes = routeContextService.getCamelRoutes();

    Optional<CamelRoute> routeOptional = routes.stream().filter(p -> p.getOutputs().stream().anyMatch(q -> q.getId().equals(endpointId))).findFirst();

    /*
    there is no way to have an empty optional because
    the endpointId should be output of one of the routes
     */

    if (routeOptional.isPresent()) {
      return routeOptional.get().getId();
    }

    return null;
  }

}
