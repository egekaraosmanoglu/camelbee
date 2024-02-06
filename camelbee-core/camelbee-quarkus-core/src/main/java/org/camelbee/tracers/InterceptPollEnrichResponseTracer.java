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
import org.camelbee.utils.ExchangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;

/**
 * Responsible for tracing the response of
 * the PollEnrich component configured in the route
 */
public class InterceptPollEnrichResponseTracer extends InterceptorTracer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptPollEnrichResponseTracer.class);

    @Override
    public void trace(Exchange exchange, MessageService messageService) {

        try {

            String responseBody = getBodyAndConvertInputStreamsToString(exchange);

            final var requestHeaders = ExchangeUtils.getHeaders(exchange);

            String toEndpointDecode = URLDecoder.decode((String) exchange.getProperty(Exchange.TO_ENDPOINT), "UTF-8");

            exchange.setProperty(CamelBeeConstants.SEND_ENDPOINT, toEndpointDecode);

            /*
             InterceptPollEnrichResponseTracer called after the pollEnrich step,
             so we need to put an empty request to the messageList for the CamelBee Application
             */
            messageService.addMessage(new Message(exchange.getExchangeId(), null, null, (String) exchange.getProperty(
                    CamelBeeConstants.CURRENT_ROUTE_NAME),
                    toEndpointDecode, MessageType.REQUEST, null));

            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

            String exception = cause != null ? cause.getLocalizedMessage() : null;

            messageService.addMessage(new Message(exchange.getExchangeId(), responseBody, requestHeaders,
                    (String) exchange.getProperty(
                            CamelBeeConstants.CURRENT_ROUTE_NAME),
                    (String) exchange.getProperty(CamelBeeConstants.SEND_ENDPOINT), MessageType.RESPONSE, exception));

        } catch (Exception e) {
            LOGGER.error("Could not trace PollEnrich Respone Exchange: {} with exception: {}", exchange, e);
        }

    }

}
