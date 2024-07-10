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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.camel.spi.CamelEvent.ExchangeCreatedEvent;
import org.apache.camel.spi.CamelEvent.ExchangeSendingEvent;
import org.apache.camel.spi.CamelEvent.ExchangeSentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * CamelBee Tracer Service collects request/responses of Camel Components.
 */
@Component
public class TracerService {

  private final long traceIdleTime;
  private final ExchangeCreatedEventTracer exchangeCreatedEventTracer;
  private final ExchangeSendingEventTracer exchangeSendingEventTracer;
  private final ExchangeSentEventTracer exchangeSentEventTracer;

  private AtomicBoolean tracingEnabled = new AtomicBoolean(true);

  private AtomicLong lastTracingActivatedTime = new AtomicLong(System.currentTimeMillis());

  /**
   * Constructor.
   *
   * @param traceIdleTime              The traceIdleTime.
   * @param exchangeCreatedEventTracer The exchangeCreatedEventTracer.
   * @param exchangeSendingEventTracer The exchangeSendingEventTracer.
   * @param exchangeSentEventTracer    The exchangeSentEventTracer.
   */
  public TracerService(@Value("${camelbee.debugger-max-idle-time:300000}") long traceIdleTime, ExchangeCreatedEventTracer exchangeCreatedEventTracer,
      ExchangeSendingEventTracer exchangeSendingEventTracer, ExchangeSentEventTracer exchangeSentEventTracer) {
    this.traceIdleTime = traceIdleTime;
    this.exchangeCreatedEventTracer = exchangeCreatedEventTracer;
    this.exchangeSendingEventTracer = exchangeSendingEventTracer;
    this.exchangeSentEventTracer = exchangeSentEventTracer;
  }

  /**
   * TraceFromRequest.
   *
   * @param exchangeCreatedEvent The exchange.
   */
  public void traceExchangeCreateEvent(ExchangeCreatedEvent exchangeCreatedEvent) {
    if (isTracingEnabled()) {
      exchangeCreatedEventTracer.traceEvent(exchangeCreatedEvent);
    }
  }

  /**
   * Trace producer endpoint calls.
   *
   * @param exchangeSendingEvent The exchange.
   */
  public void traceExchangeSendingEvent(ExchangeSendingEvent exchangeSendingEvent) {
    if (isTracingEnabled()) {
      exchangeSendingEventTracer.traceEvent(exchangeSendingEvent);
    }
  }

  /**
   * traceInterceptSendToEndpointResponse.
   *
   * @param exchangeSentEvent The exchange.
   */
  public void traceExchangeSentEvent(ExchangeSentEvent exchangeSentEvent) {
    if (isTracingEnabled()) {
      exchangeSentEventTracer.traceEvent(exchangeSentEvent);
    }
  }

  /**
   * isTracingEnabled.
   *
   * @return boolean The tracing status.
   */
  public boolean isTracingEnabled() {
    // if CamelBee WebGL application is not active anymore and not calling the keepTracingActive api
    if (tracingEnabled.get() && System.currentTimeMillis() - lastTracingActivatedTime.get() > traceIdleTime) {
      tracingEnabled.set(true);
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
