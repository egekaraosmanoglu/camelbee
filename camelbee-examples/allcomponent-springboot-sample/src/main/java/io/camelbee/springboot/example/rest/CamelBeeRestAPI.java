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
package io.camelbee.springboot.example.rest;

import io.camelbee.springboot.example.model.rest.Musician;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public final class CamelBeeRestAPI extends RouteBuilder {

    @Autowired
    CamelBeeRouteConfigurer camelBeeRouteConfigurer;

    /**
     * Defines Apache Camel routes using REST DSL fluent API.
     * Normally this rest configuration should be created by Camel Rest Dsl generator.
     */
    public void configure() throws Exception {

        camelBeeRouteConfigurer.configureRoute(this);

        restConfiguration().component("servlet").clientRequestValidation(true);

        rest()
                .post("/testapi/v1/musician")
                .id("postMusician")
                .type(Musician.class)
                .description("Save a musician")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:postMusician");

        rest()
                .get("/testapi/v1/musician")
                .id("getMusician")
                .description("Get a musician")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:getMusician");

    }
}
