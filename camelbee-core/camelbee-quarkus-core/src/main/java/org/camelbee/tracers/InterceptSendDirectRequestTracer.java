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
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Responsible for tracing Send Direct Requests triggered by the endpoint interceptor configured in the
 * route configuration {@see CamelBeeRouteConfigurer.configure}.
 */
public class InterceptSendDirectRequestTracer extends RequestResponseTracer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptSendDirectRequestTracer.class);

    @Override
    public void trace(Exchange exchange, MessageService messageService) {

        try {

            String httpRequestBody = null;

            if (exchange.getIn().getBody() != null) {

                httpRequestBody = exchange.getIn().getBody(String.class);

            }

            Deque<String> stack = (Deque<String>) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK);

            // happens only if a route is called via the producertemplate.
            if (stack == null) {
                stack = new ArrayDeque<>();
            }

            Deque<String> clonedStack = new ArrayDeque<>(stack);

            clonedStack.peek();

            final String currentRouteProperty = (String) exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME);

            final String currentRoute = (String) exchange.getProperty(Exchange.TO_ENDPOINT);

            clonedStack.push(currentRoute);

            exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_TRACE_STACK, clonedStack);

            exchange.setProperty(CamelBeeConstants.CURRENT_ROUTE_NAME, currentRoute);

            final var requestHeaders = getHeaders(exchange);

            messageService
                    .addMessage(new Message(exchange.getExchangeId(), httpRequestBody, requestHeaders, currentRouteProperty,
                            currentRoute, MessageType.REQUEST, null));

        } catch (Exception e) {
            LOGGER.error("Could not trace Send Direct Request Exchange: {} with exception: {}", exchange, e);
        }

    }

}
