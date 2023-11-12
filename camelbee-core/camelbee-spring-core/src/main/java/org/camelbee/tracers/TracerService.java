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
import org.camelbee.debugger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CamelBee Tracer Service collects request/responses of Camel Components.
 */
@Component(TracerService.CAMELBEE_TRACER)
public class TracerService {

    @Value("${camelbee.debugger-max-idle-time:300000}")
    private long traceIdleTime;

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

    private AtomicBoolean tracingEnabled = new AtomicBoolean(false);

    private AtomicLong lastTracingActivatedTime = new AtomicLong(System.currentTimeMillis());

    @Autowired
    MessageService messageService;

    public void traceFromDirectRequest(Exchange exchange) {
        if (isTracingEnabled()) {
            fromDirectTracer.trace(exchange, messageService);
        }
    }

    public void traceInterceptSendToEndpointRequest(Exchange exchange) {
        if (isTracingEnabled()) {
            interceptSendToRequestTracer.trace(exchange, messageService);
        }
    }

    public void traceInterceptSendToEndpointResponse(Exchange exchange) {
        if (isTracingEnabled()) {
            interceptSendToResponseTracer.trace(exchange, messageService);
        }
    }

    public void traceInterceptSendDirectEndpointRequest(Exchange exchange) {
        if (isTracingEnabled()) {
            interceptSendDirectRequestTracer.trace(exchange, messageService);
        }
    }

    public void traceInterceptSendDirectEndpointResponse(Exchange exchange) {
        if (isTracingEnabled()) {
            interceptSendDirectResponseTracer.trace(exchange, messageService);
        }
    }

    public boolean isTracingEnabled() {
        // if CamelBee WebGL application is not active anymore and not calling the keepTracingActive api
        if (tracingEnabled.get() && System.currentTimeMillis() - lastTracingActivatedTime.get() > traceIdleTime) {
            tracingEnabled.set(false);
        }
        return tracingEnabled.get();
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled.set(tracingEnabled);
    }

    public void keepTracingActive() {
        lastTracingActivatedTime.set(System.currentTimeMillis());
    }


}
