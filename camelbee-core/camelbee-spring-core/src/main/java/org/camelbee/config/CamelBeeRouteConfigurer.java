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

import org.apache.camel.builder.RouteBuilder;
import org.camelbee.tracers.TracerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The route configurer which sets all listeners, interceptors and the MDCUnitOfWork.
 */
@Component
public class CamelBeeRouteConfigurer {

    @Value("${camelbee.context-enabled:false}")
    private boolean contextEnabled;

    @Value("${camelbee.debugger-enabled:false}")
    private boolean debuggerEnabled;

    /**
     * Configures a route for a CamelBee enabled Camel application.
     *
     * @param routeBuilder The routebuilder to be configured.
     * @throws Exception The exception.
     */
    public void configureRoute(RouteBuilder routeBuilder) {

        // do not intercept and cache the messages
        if (!contextEnabled || !debuggerEnabled)
            return;

        routeBuilder.getContext().setStreamCaching(true);
        routeBuilder.getContext().setUseMDCLogging(true);

        // add interceptor for from direct
        routeBuilder.interceptFrom("*").bean(TracerService.CAMELBEE_TRACER,
                TracerService.TRACE_FROM_DIRECT_REQUEST).afterPropertiesSet();

        // add interceptor for components other than direct|seda|servlet
        routeBuilder.interceptSendToEndpoint("(?!direct|seda|servlet).+").bean(TracerService.CAMELBEE_TRACER,
                        TracerService.TRACE_INTERCEPT_SEND_TO_REQUEST)
                .afterUri("bean:%s?method=%s".formatted(TracerService.CAMELBEE_TRACER, TracerService.TRACE_INTERCEPT_SEND_TO_RESPONSE));

        // add interceptor for direct and seda components
        routeBuilder.interceptSendToEndpoint("^(direct|seda).*").bean(TracerService.CAMELBEE_TRACER,
                        TracerService.TRACE_INTERCEPT_SEND_DIRECT_REQUEST)
                .afterUri("bean:%s?method=%s".formatted(TracerService.CAMELBEE_TRACER, TracerService.TRACE_INTERCEPT_SEND_DIRECT_RESPONSE));


    }


}
