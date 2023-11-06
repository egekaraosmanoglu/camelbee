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

import java.net.URLDecoder;

/**
 * Responsible for tracing Send To Requests triggered by
 * the endpoint interceptor configured in the route configuration
 * {@see CamelBeeRouteConfigurer.configure}.
 */
public class InterceptSendToRequestTracer extends RequestResponseTracer {

    /**
     * The logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(InterceptSendToRequestTracer.class);

    @Override
    public void trace(Exchange exchange, MessageService messageService) {

        try {

            /*
             endpoint called from ProducerController is also intercepted here
             which we should not put into the messages
             */
            if (exchange.getProperty(CamelBeeConstants.CURRENT_ROUTE_NAME) == null) {
                return;
            }

            String httpRequestBody = null;

            if (exchange.getIn().getBody() != null) {

                httpRequestBody = exchange.getIn().getBody(String.class);

            }

            final var requestHeaders = getHeaders(exchange);

            String toEndpointDecode = URLDecoder.decode((String) exchange.getProperty(Exchange.TO_ENDPOINT), "UTF-8");

            exchange.setProperty(CamelBeeConstants.SEND_ENDPOINT, toEndpointDecode);

            messageService.addMessage(new Message(exchange.getExchangeId(), httpRequestBody, requestHeaders, (String) exchange.getProperty(
                    CamelBeeConstants.CURRENT_ROUTE_NAME),
                    toEndpointDecode, MessageType.REQUEST, null));

        } catch (Exception e) {
            LOGGER.error("Could not trace Send To Request Exchange: {} with exception: {}", exchange, e);
        }

    }


}
