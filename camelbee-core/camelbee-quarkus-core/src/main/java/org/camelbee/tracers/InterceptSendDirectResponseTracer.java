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
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.cxf.message.MessageContentsList;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Responsible for tracing Send Direct Responses via logger beans.
 */
public class InterceptSendDirectResponseTracer extends RequestResponseTracer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptSendDirectResponseTracer.class);

    @Override
    public void trace(Exchange exchange, MessageService messageService) {

        try {

            String httpResponseBody = null;

            if (exchange.getIn().getBody() instanceof MessageContentsList msgList) {

                httpResponseBody = !msgList.isEmpty() ? msgList.get(0).toString() : null;

            } else if (exchange.getIn().getBody() instanceof InputStreamCache requestStreamCache) {

                httpResponseBody = new String(requestStreamCache.readAllBytes());

                exchange.getIn().setBody(httpResponseBody);

            } else if (exchange.getIn().getBody() instanceof InputStream requestStream) {

                httpResponseBody = new String(requestStream.readAllBytes());

                exchange.getIn().setBody(httpResponseBody);

            } else if (exchange.getIn().getBody() != null) {
                httpResponseBody = exchange.getIn().getBody(String.class);
            }

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

            final var requestHeaders = getHeaders(exchange);

            messageService.addMessage(new Message(exchange.getExchangeId(), httpResponseBody, requestHeaders, callerRoute,
                    currentRoute, MessageType.RESPONSE, null));

        } catch (Exception e) {
            LOGGER.error("Could not trace Send Direct Response Exchange: {} with exception: {}", exchange, e);
        }

    }
}
