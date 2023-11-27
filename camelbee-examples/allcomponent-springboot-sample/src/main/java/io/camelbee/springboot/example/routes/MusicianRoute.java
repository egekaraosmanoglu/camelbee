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
package io.camelbee.springboot.example.routes;

import io.camelbee.springboot.example.constants.Constants;
import io.camelbee.springboot.example.exception.ExceptionHandler;
import io.camelbee.springboot.example.model.jpa.SongEntity;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.camelbee.tracers.TracerService;
import org.springframework.stereotype.Component;

/**
 * Musician Route.
 *
 * @author ekaraosmanoglu
 */
@Component
public class MusicianRoute extends RouteBuilder {

    private static final String MUSICIAN_PROCESSOR_ROUTE = "direct:musicianProcessor";

    final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
    final ExceptionHandler genericExceptionHandler;


    public MusicianRoute(CamelBeeRouteConfigurer camelBeeRouteConfigurer, ExceptionHandler genericExceptionHandler) {
        this.camelBeeRouteConfigurer = camelBeeRouteConfigurer;
        this.genericExceptionHandler = genericExceptionHandler;
    }

    @Override
    public void configure() throws Exception {

        camelBeeRouteConfigurer.configureRoute(this);

        errorHandler(genericExceptionHandler.appErrorHandler());

        from("direct:postMusician").routeId("postMusicianRoute")
                .to("bean-validator://camelbee")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("direct:getMusician").routeId("getMusicianRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("file://inputdir/?delete=true").routeId("fileListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("timer://foo?fixedRate=true&period=500000").routeId("timerRoute")
                .setBody(constant("timerTestMessage"))
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("kafka:camelbee-northbound-topic").routeId("kafkaListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("paho-mqtt5:camelbee-northbound-topic?brokerUrl=tcp://localhost:1883")
                .routeId("mqttListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("jpa:io.camelbee.springboot.example.model.jpa.MusicianEntity?"
                + "namedQuery=getMusicians&delay=5s&consumeDelete=true&maximumResults=5")
                .routeId("jpaListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("jms:queue::camelbee-northbound-queue"
                + "?disableReplyTo=true&jmsMessageType=Text")
                .routeId("jmsListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("mongodb:mongoBean?database=camelbee&collection=musicians-in&createCollection=true")
                .routeId("mongodbListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from("spring-rabbitmq:cheese?queues=camelbee-northbound-queue&routingKey=musicians")
                .routeId("rabbitmqListenerRoute")
                .to(MUSICIAN_PROCESSOR_ROUTE);

        from(MUSICIAN_PROCESSOR_ROUTE).routeId("musicianProcessorRoute")
                .setProperty(Constants.ORIGINAL_BODY, body())
                .to("direct:invokeHttpBin")
                .to("direct:invokeKafka")
                .wireTap("direct:invokeMqtt")
                .multicast().parallelProcessing()
                .to("direct:invokeMockA")
                .to("direct:invokeMockB")
                .end()
                .enrich("direct:invokeJms")
                .enrich().constant("direct:invokeMongoDb")
                .recipientList().constant("direct:invokeJpa,direct:invokeFile")
                .routingSlip().constant("direct:invokeMockA,direct:invokeMockB")
                .removeHeaders("*")
                .toD("direct:invokeRabbitMq")
                .pollEnrich("jms:queue:camelbee-southhbound-queue", 1000, (original, resource) -> resource)
                .bean(TracerService.CAMELBEE_TRACER, TracerService.TRACE_INTERCEPT_POLLENRICH_RESPONSE)
                .to("direct:invokeMockC");


        from("direct:invokeHttpBin").routeId("invokeHttpBinRoute")
                .marshal().json()
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("hhId", constant("2"))
                .toD("http:{{httpbin-api.url}}/${header.hhId}?bridgeEndpoint=true")
                .id("httpBinEndpoint");

        from("direct:invokeKafka").routeId("invokeKafkaRoute")
                .to("kafka:camelbee-southbound-topic")
                .id("kafkaEndpoint");

        from("direct:invokeMqtt").routeId("invokeMqttRoute")
                .to("paho-mqtt5:camelbee-southbound-topic?brokerUrl=tcp://localhost:1883")
                .id("mqttEndpoint");

        from("direct:invokeRabbitMq").routeId("invokeRabbitMqRoute")
                .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
                .convertBodyTo(String.class)
                .to(ExchangePattern.InOnly, "spring-rabbitmq:cheese?routingKey=songs")
                .id("rabbitMqEndpoint");


        from("direct:invokeMongoDb").routeId("invokeMongoDbRoute")
                .to("mongodb:mongoBean?database=camelbee&collection=musicians-out&operation=insert")
                .id("mongoDbEndpoint");

        from("direct:invokeJms").routeId("invokeJmsRoute")
                .to("jms:queue:camelbee-southhbound-queue?disableReplyTo=true&jmsMessageType=Text")
                .id("jmsEndpoint");

        from("direct:invokeJpa").routeId("invokeJpaRoute")
                .process(e -> e.getIn().setBody(new SongEntity()))
                .to("jpa:io.camelbee.springboot.example.model.jpa.SongEntity")
                .id("jpaEndpoint");

        from("direct:invokeFile").routeId("invokeFileRoute")
                .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
                .convertBodyTo(String.class)
                .to("file://outputdir")
                .id("fileEndpoint");

        from("direct:invokeMockA").routeId("invokeMockARoute")
                .to("mock:A")
                .id("mockAEndpoint");

        from("direct:invokeMockB").routeId("invokeMockBRoute")
                .to("mock:B")
                .id("mockBEndpoint");

        from("direct:invokeMockC").routeId("invokeMockCRoute")
                .to("mock:C")
                .id("mockCEndpoint");

    }
}
