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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.camel.impl.event.ExchangeCreatedEvent;
import org.apache.camel.impl.event.ExchangeSendingEvent;
import org.apache.camel.impl.event.ExchangeSentEvent;
import org.apache.camel.spi.CamelEvent.ExchangeCompletedEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CamelBee Tracer Service collects request/responses of Camel Components.
 */
@ApplicationScoped
@RegisterForReflection(fields = false)
public class TracerService {

  private final long traceIdleTime;
  private final ExchangeCreatedEventTracer exchangeCreatedEventTracer;
  private final ExchangeSendingEventTracer exchangeSendingEventTracer;
  private final ExchangeSentEventTracer exchangeSentEventTracer;
  private final ExchangeCompletedEventTracer exchangeCompletedEventTracer;

  private AtomicBoolean tracingEnabled = new AtomicBoolean(false);

  private AtomicLong lastTracingActivatedTime = new AtomicLong(System.currentTimeMillis());

  /**
   * Constructor.
   *
   * @param traceIdleTime                The traceIdleTime.
   * @param exchangeCreatedEventTracer   The exchangeCreatedEventTracer.
   * @param exchangeSendingEventTracer   The exchangeSendingEventTracer.
   * @param exchangeSentEventTracer      The exchangeSentEventTracer.
   * @param exchangeCompletedEventTracer The exchangeCompletedEventTracer.
   */
  public TracerService(@ConfigProperty(name = "camelbee.debugger-max-idle-time", defaultValue = "300000") long traceIdleTime,
      ExchangeCreatedEventTracer exchangeCreatedEventTracer,
      ExchangeSendingEventTracer exchangeSendingEventTracer, ExchangeSentEventTracer exchangeSentEventTracer,
      ExchangeCompletedEventTracer exchangeCompletedEventTracer) {
    this.traceIdleTime = traceIdleTime;
    this.exchangeCreatedEventTracer = exchangeCreatedEventTracer;
    this.exchangeSendingEventTracer = exchangeSendingEventTracer;
    this.exchangeSentEventTracer = exchangeSentEventTracer;
    this.exchangeCompletedEventTracer = exchangeCompletedEventTracer;
  }

  /**
   * traceExchangeCreateEvent.
   *
   * @param exchangeCreatedEvent The exchange.
   */
  public void traceExchangeCreateEvent(ExchangeCreatedEvent exchangeCreatedEvent) {
    if (isTracingEnabled()) {
      exchangeCreatedEventTracer.traceEvent(exchangeCreatedEvent);
    }

  }

  /**
   * traceExchangeSendingEvent.
   *
   * @param exchangeSendingEvent The exchange.
   */
  public void traceExchangeSendingEvent(ExchangeSendingEvent exchangeSendingEvent) {
    if (isTracingEnabled()) {
      exchangeSendingEventTracer.traceEvent(exchangeSendingEvent);
    }

  }

  /**
   * traceExchangeSentEvent.
   *
   * @param exchangeSentEvent The exchange.
   */
  public void traceExchangeSentEvent(ExchangeSentEvent exchangeSentEvent) {
    if (isTracingEnabled()) {
      exchangeSentEventTracer.traceEvent(exchangeSentEvent);
    }
  }

  /**
   * traceExchangeCompletedEvent.
   *
   * @param exchangeCompletedEvent The exchange.
   */
  public void traceExchangeCompletedEvent(ExchangeCompletedEvent exchangeCompletedEvent) {
    if (isTracingEnabled()) {
      exchangeCompletedEventTracer.traceEvent(exchangeCompletedEvent);
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
