package org.camelbee.notifier;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.impl.event.ExchangeCreatedEvent;
import org.apache.camel.impl.event.ExchangeSendingEvent;
import org.apache.camel.impl.event.ExchangeSentEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.camelbee.tracers.EventNotifier;
import org.camelbee.tracers.TracerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CamelBeeEventNotifier extends EventNotifierSupport {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CamelBeeEventNotifier.class);

  final TracerService tracerService;

  public CamelBeeEventNotifier(TracerService tracerService) {
    this.tracerService = tracerService;
  }

  @Override
  public void notify(CamelEvent event) throws Exception {

    switch(event){
    case ExchangeCreatedEvent exchangeCreatedEvent:
     tracerService.traceExchangeCreateEvent(exchangeCreatedEvent);
     break;
    case ExchangeSendingEvent exchangeSendingEvent:
      tracerService.traceExchangeSendingEvent(exchangeSendingEvent);
      break;
    case ExchangeSentEvent exchangeSentEvent:
      tracerService.traceExchangeSentEvent(exchangeSentEvent);
      break;
    case ExchangeCompletedEvent exchangeCompletedEvent:
      tracerService.traceExchangeCompletedEvent(exchangeCompletedEvent);
      break;
    default:
        LOGGER.trace("Event type not traced: {}", event.getClass().getName());
    }

    // notify attached observers
    notifyAll(event);
  }

  private List<EventNotifier> notifiers  = new ArrayList<>();

  public void addNotifier(EventNotifier notifier) {
    notifiers.add(notifier);
  }

  public void removeNotifier(EventNotifier notifier) {
    notifiers.remove(notifier);
  }

  private void notifyAll(CamelEvent event) {
    for (EventNotifier notifier : notifiers) {
      notifier.notify(event);
    }
  }
}
