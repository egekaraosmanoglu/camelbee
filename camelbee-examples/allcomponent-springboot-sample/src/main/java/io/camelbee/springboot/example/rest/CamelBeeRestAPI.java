package io.camelbee.springboot.example.rest;

import io.camelbee.springboot.example.model.rest.Musician;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public final class CamelBeeRestAPI extends RouteBuilder {

  /**
   * Defines Apache Camel routes using REST DSL fluent API.
   * Normally this rest configuration should be created by Camel Rest Dsl generator.
   */
  public void configure() throws Exception {

    CamelBeeRouteConfigurer.configure(this);

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
