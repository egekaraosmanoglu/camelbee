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
package io.camelbee.quarkus.example.rest;

import io.camelbee.quarkus.example.model.rest.Musician;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
public final class CamelBeeRestAPI extends RouteBuilder {

    @Inject
    CamelBeeRouteConfigurer camelBeeRouteConfigurer;

    /**
     * Defines Apache Camel routes using REST DSL fluent API.
     * Normally this rest configuration should be created by Camel Rest Dsl generator.
     */
    public void configure() throws Exception {

        camelBeeRouteConfigurer.configureRoute(this);

        restConfiguration().bindingMode(RestBindingMode.json);

        rest()
                .post("/testapi/v1/musician")
                .id("postMusician")
                .type(Musician.class)
                .description("Save a musician")
                .consumes(MediaType.APPLICATION_JSON)
                .produces(MediaType.APPLICATION_JSON)
                .to("direct:postMusician");

        rest()
                .get("/testapi/v1/musician")
                .id("getMusician")
                .description("Get a musician")
                .produces(MediaType.APPLICATION_JSON)
                .to("direct:getMusician");

    }
}
