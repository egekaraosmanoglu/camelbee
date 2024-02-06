/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camelbee.tracers;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.SendProcessor;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Responsible for tracing exchanges when an ExchangeFailureHandlingEvent is triggered.
 */
public class ExchangeFailureHandlingEventTracer extends CamelEventTracer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeFailureHandlingEventTracer.class);

    @Override
    public void trace(Exchange exchange, Processor failureHandler, MessageService messageService) {

        try {

            // error handlerroute will handle exception and trace the messages with the senddirect tracers
            if (failureHandler instanceof SendProcessor) {
                return;
            }

            String responseBody = getBodyAndConvertInputStreamsToString(exchange);

            Deque<String> stack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);
            Deque<String> clonedStack = new ArrayDeque<>(stack);

            final String callerRoute = clonedStack.peek();

            String exceptionHandlerRouteId = null;

            if (failureHandler instanceof Pipeline pipeline) {
                exceptionHandlerRouteId = pipeline.getRouteId();
            }

            String handlerRouteEndpoint = "";

            if (exceptionHandlerRouteId != null) {
                handlerRouteEndpoint = ((ModelCamelContext) exchange.getContext()).getRouteDefinition(exceptionHandlerRouteId)
                        .getEndpointUrl();

                handlerRouteEndpoint = handlerRouteEndpoint.replace(":", "://");
                // loop while finding the handlerRouteEndpoint
                while (!clonedStack.isEmpty() && !handlerRouteEndpoint.equals(clonedStack.peek())) {
                    clonedStack.pop();
                }
            } else {
                handlerRouteEndpoint = callerRoute;
            }

            InterceptorType interceptorType = (InterceptorType) exchange
                    .getProperty(CamelBeeConstants.CURRENT_INTERCEPTOR_TYPE);

            if (interceptorType != null && interceptorType.equals(InterceptorType.DIRECT_INTERCEPTOR)) {

                handleDirectResponse(exchange, messageService, responseBody, clonedStack, callerRoute, handlerRouteEndpoint);

            } else if (interceptorType != null && interceptorType.equals(InterceptorType.ENDPOINT_INTERCEPTOR)) {

                handleEndpointResponse(exchange, messageService, responseBody, clonedStack, handlerRouteEndpoint);

            }
        } catch (Exception e) {
            LOGGER.error("Could not trace ExchangeFailureHandlingEvent Exchange: {} with exception: {}", exchange, e);
        }

    }

    private void handleEndpointResponse(Exchange exchange, MessageService messageService, String httpResponseBody,
            Deque<String> clonedStack,
            String handlerRouteEndpoint) {

        /*
        endpoint called from ProducerController is also intercepted here
        which we should not put into the messages
        */
        if (exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME) == null) {
            return;
        }

        exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedStack);

        var requestHeaders = ExchangeUtils.getHeaders(exchange);

        if (exchange.getProperty(CamelBeeConstants.CAMEL_HTTP_PATH_HEADER) != null) {
            requestHeaders += "\nCamelHttpPath:" + exchange.getProperty(CamelBeeConstants.CAMEL_HTTP_PATH_HEADER);
        }

        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        String exception = cause != null ? cause.getLocalizedMessage() : null;

        messageService.addMessage(new Message(exchange.getExchangeId(), httpResponseBody, requestHeaders, handlerRouteEndpoint,
                (String) exchange.getProperty(CamelBeeConstants.SEND_ENDPOINT), MessageType.ERROR_RESPONSE, exception));

    }

    private void handleDirectResponse(Exchange exchange, MessageService messageService, String httpResponseBody,
            Deque<String> clonedStack, String callerRoute, String handlerRouteEndpoint) {

        exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedStack);

        /*
         set the previous route (callerRoute) as the current route
         which would be used in SendToRequestTracers
         */
        exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, callerRoute);

        var requestHeaders = ExchangeUtils.getHeaders(exchange);

        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        String exception = cause != null ? cause.getLocalizedMessage() : null;

        messageService.addMessage(new Message(exchange.getExchangeId(), httpResponseBody, requestHeaders, callerRoute,
                handlerRouteEndpoint, MessageType.ERROR_RESPONSE, exception));
    }
}
