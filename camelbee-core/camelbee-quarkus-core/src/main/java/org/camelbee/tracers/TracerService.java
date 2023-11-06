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

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.Exchange;
import org.camelbee.debugger.service.MessageService;

/**
 * CamelBee Tracer Service collects request/responses of Camel Components.
 */
@ApplicationScoped
@Named(TracerService.CAMELBEE_TRACER)
@RegisterForReflection(fields = false)
public class TracerService {

    public static final String CAMELBEE_TRACER = "camelBeeTracer";

    public static final String TRACE_FROM_DIRECT_REQUEST = "traceFromDirectRequest";

    public static final String TRACE_INTERCEPT_SEND_DIRECT_REQUEST = "traceInterceptSendDirectEndpointRequest";
    public static final String TRACE_INTERCEPT_SEND_DIRECT_RESPONSE = "traceInterceptSendDirectEndpointResponse";

    public static final String TRACE_INTERCEPT_SEND_TO_REQUEST = "traceInterceptSendToEndpointRequest";
    public static final String TRACE_INTERCEPT_SEND_TO_RESPONSE = "traceInterceptSendToEndpointResponse";

    private FromDirectTracer fromDirectTracer = new FromDirectTracer();

    private InterceptSendDirectRequestTracer interceptSendDirectRequestTracer = new InterceptSendDirectRequestTracer();
    private InterceptSendDirectResponseTracer interceptSendDirectResponseTracer = new InterceptSendDirectResponseTracer();

    private InterceptSendToRequestTracer interceptSendToRequestTracer = new InterceptSendToRequestTracer();
    private InterceptSendToResponseTracer interceptSendToResponseTracer = new InterceptSendToResponseTracer();

    @Inject
    MessageService messageService;

    public void traceFromDirectRequest(Exchange exchange) {
        fromDirectTracer.trace(exchange, messageService);
    }

    public void traceInterceptSendToEndpointRequest(Exchange exchange) {
        interceptSendToRequestTracer.trace(exchange, messageService);
    }

    public void traceInterceptSendToEndpointResponse(Exchange exchange) {
        interceptSendToResponseTracer.trace(exchange, messageService);
    }

    public void traceInterceptSendDirectEndpointRequest(Exchange exchange) {
        interceptSendDirectRequestTracer.trace(exchange, messageService);
    }

    public void traceInterceptSendDirectEndpointResponse(Exchange exchange) {
        interceptSendDirectResponseTracer.trace(exchange, messageService);
    }

}
