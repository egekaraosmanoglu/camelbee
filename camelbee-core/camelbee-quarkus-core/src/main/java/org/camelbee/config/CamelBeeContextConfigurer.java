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
package org.camelbee.config;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.ExchangeFailureHandlingEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.tracers.TracerService;

@ApplicationScoped
public class CamelBeeContextConfigurer {

    @Produces
    @ApplicationScoped
    @Startup
    public EventNotifierSupport eventNotifier(CamelContext camelContext, TracerService tracerService) {
        EventNotifierSupport eventNotifier = new EventNotifierSupport() {
            @Override
            public void notify(CamelEvent event) throws Exception {

                /*
                 when exceptions are handled with a doTry()..doCatch() block
                 then afterUri of the interceptors are not triggered.
                 so we need to handle the response messages with the eventNotifier
                 */
                if (event instanceof ExchangeFailureHandlingEvent failedEvent) {

                    Exchange exchange = failedEvent.getExchange();

                    Integer eventIdentityHashCode = System.identityHashCode(failedEvent);

                    Object previousEventIdentityHashCode = exchange
                            .getProperty(CamelBeeConstants.CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE);

                    /*
                     in quarkus the same error triggered twice, so we need to check if
                     this failureEvent has already been traced!
                     This is not happening in SpringBoot.
                     */
                    if (previousEventIdentityHashCode != null && eventIdentityHashCode.equals(previousEventIdentityHashCode)) {
                        return;
                    }

                    exchange.setProperty(CamelBeeConstants.CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE, eventIdentityHashCode);

                    tracerService.traceExchangeFailureHandlingEvent(exchange, failedEvent.getFailureHandler());

                }
            }

            @Override
            protected void doStart() throws Exception {
                setIgnoreCamelContextEvents(true);
                setIgnoreRouteEvents(true);
                setIgnoreServiceEvents(true);
            }

        };

        camelContext.getManagementStrategy().addEventNotifier(eventNotifier);
        return eventNotifier;
    }
}
