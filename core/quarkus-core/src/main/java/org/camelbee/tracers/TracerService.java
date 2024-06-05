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

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.camelbee.debugger.service.MessageService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CamelBee Tracer Service collects request/responses of Camel Components.
 */
@ApplicationScoped
@Named(TracerService.CAMELBEE_TRACER)
@RegisterForReflection(fields = false)
public class TracerService {

  @ConfigProperty(name = "camelbee.debugger-max-idle-time", defaultValue = "300000")
  private long traceIdleTime;

  public static final String CAMELBEE_TRACER = "camelBeeTracer";

  public static final String TRACE_INTERCEPT_FROM_REQUEST = "traceFromRequest";

  public static final String TRACE_INTERCEPT_POLLENRICH_RESPONSE = "traceInterceptPollEnrichResponse";

  public static final String TRACE_INTERCEPT_SEND_DIRECT_REQUEST = "traceInterceptSendDirectEndpointRequest";
  public static final String TRACE_INTERCEPT_SEND_DIRECT_RESPONSE = "traceInterceptSendDirectEndpointResponse";

  public static final String TRACE_INTERCEPT_SEND_TO_REQUEST = "traceInterceptSendToEndpointRequest";
  public static final String TRACE_INTERCEPT_SEND_TO_RESPONSE = "traceInterceptSendToEndpointResponse";

  private InterceptFromTracer interceptFromTracer = new InterceptFromTracer();
  private InterceptPollEnrichResponseTracer interceptPollEnrichResponseTracer = new InterceptPollEnrichResponseTracer();

  private InterceptSendDirectRequestTracer interceptSendDirectRequestTracer = new InterceptSendDirectRequestTracer();
  private InterceptSendDirectResponseTracer interceptSendDirectResponseTracer = new InterceptSendDirectResponseTracer();

  private InterceptSendToRequestTracer interceptSendToRequestTracer = new InterceptSendToRequestTracer();
  private InterceptSendToResponseTracer interceptSendToResponseTracer = new InterceptSendToResponseTracer();

  private ExchangeFailureHandlingEventTracer exchangeFailureHandlingEventTracer = new ExchangeFailureHandlingEventTracer();

  private AtomicBoolean tracingEnabled = new AtomicBoolean(false);

  private AtomicLong lastTracingActivatedTime = new AtomicLong(System.currentTimeMillis());

  @Inject
  MessageService messageService;

  /**
   * TraceFromRequest.
   *
   * @param exchange The exchange.
   */
  public void traceFromRequest(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptFromTracer.trace(exchange, messageService);
    }

    interceptFromTracer.visit(exchange);
  }

  /**
   * Accepts Tracer visitors for custom logic.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceFromRequestVisitor(TracerVisitor tracerVisitor) {
    interceptFromTracer.accept(tracerVisitor);
  }

  /**
   * Trace poolEnrich component response.
   *
   * @param exchange The exchange.
   */
  public void traceInterceptPollEnrichResponse(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptPollEnrichResponseTracer.trace(exchange, messageService);
    }

    interceptPollEnrichResponseTracer.visit(exchange);
  }

  /**
   * Accepts Tracer visitors for custom logic.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceInterceptPollEnrichResponseVisitor(TracerVisitor tracerVisitor) {
    interceptPollEnrichResponseTracer.accept(tracerVisitor);
  }

  /**
   * Trace producer endpoint calls.
   *
   * @param exchange The exchange.
   */
  public void traceInterceptSendToEndpointRequest(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptSendToRequestTracer.trace(exchange, messageService);
    }
    interceptSendToRequestTracer.visit(exchange);
  }

  /**
   * Accepts Tracer visitors for custom logic for calling endpoint.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceInterceptSendToEndpointRequestVisitor(TracerVisitor tracerVisitor) {
    interceptSendToRequestTracer.accept(tracerVisitor);
  }

  /**
   * traceInterceptSendToEndpointResponse.
   *
   * @param exchange The exchange.
   */
  public void traceInterceptSendToEndpointResponse(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptSendToResponseTracer.trace(exchange, messageService);
    }
    interceptSendToResponseTracer.visit(exchange);
  }

  /**
   * acceptTraceInterceptSendToEndpointResponseVisitor.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceInterceptSendToEndpointResponseVisitor(TracerVisitor tracerVisitor) {
    interceptSendToResponseTracer.accept(tracerVisitor);
  }

  /**
   * traceInterceptSendDirectEndpointRequest.
   *
   * @param exchange The exchange.
   */
  public void traceInterceptSendDirectEndpointRequest(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptSendDirectRequestTracer.trace(exchange, messageService);
    }
    interceptSendDirectRequestTracer.visit(exchange);
  }

  /**
   * acceptTraceInterceptSendDirectEndpointRequestVisitor.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceInterceptSendDirectEndpointRequestVisitor(TracerVisitor tracerVisitor) {
    interceptSendDirectRequestTracer.accept(tracerVisitor);
  }

  /**
   * traceInterceptSendDirectEndpointResponse.
   *
   * @param exchange The exchange.
   */
  public void traceInterceptSendDirectEndpointResponse(Exchange exchange) {
    if (isTracingEnabled()) {
      interceptSendDirectResponseTracer.trace(exchange, messageService);
    }
    interceptSendDirectResponseTracer.visit(exchange);
  }

  /**
   * acceptTraceInterceptSendDirectEndpointResponseVisitor.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceInterceptSendDirectEndpointResponseVisitor(TracerVisitor tracerVisitor) {
    interceptSendDirectResponseTracer.accept(tracerVisitor);
  }

  /**
   * traceExchangeFailureHandlingEvent.
   *
   * @param exchange       The exchange.
   * @param failureHandler The failureHandler.
   */
  public void traceExchangeFailureHandlingEvent(Exchange exchange, Processor failureHandler) {
    if (isTracingEnabled()) {
      exchangeFailureHandlingEventTracer.trace(exchange, failureHandler, messageService);
    }
    exchangeFailureHandlingEventTracer.visit(exchange);
  }

  /**
   * acceptTraceExchangeFailureHandlingEventVisitor.
   *
   * @param tracerVisitor The tracerVisitor.
   */
  public void acceptTraceExchangeFailureHandlingEventVisitor(TracerVisitor tracerVisitor) {
    exchangeFailureHandlingEventTracer.accept(tracerVisitor);
  }

  /**
   * isTracingEnabled.
   *
   * @return boolean The tracing status.
   */
  public boolean isTracingEnabled() {
    // if CamelBee WebGL application is not active anymore and not calling the keepTracingActive api
    if (tracingEnabled.get() && System.currentTimeMillis() - lastTracingActivatedTime.get() > traceIdleTime) {
      tracingEnabled.set(false);
    }
    return tracingEnabled.get();
  }

  /**
   * setTracingEnabled.
   *
   * @param tracingEnabled The tracins status.
   */
  public void setTracingEnabled(boolean tracingEnabled) {
    this.tracingEnabled.set(tracingEnabled);
  }

  /**
   * keepTracingActive.
   */
  public void keepTracingActive() {
    lastTracingActivatedTime.set(System.currentTimeMillis());
  }

}
