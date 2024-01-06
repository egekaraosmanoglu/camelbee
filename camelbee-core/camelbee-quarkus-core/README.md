# CamelBee Quarkus Core Library

## Introduction

The camelbee-quarkus-core library is an essential component for integrating a Camel Quarkus Microservice with the CamelBee WebGL application (https://www.camelbee.io). 
This library provides the necessary functionalities to configure Camel routes with interceptors, allowing comprehensive tracing of messages exchanged between the routes. 
Additionally, it includes rest resources for seamless interaction with the CamelBee WebGL application.

## Manual Installation

To manually install the core library, follow the steps below:

### Maven Installation

`mvn clean install`

Once the Maven artifact is created, you can include it in your project by adding the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>io.camelbee</groupId>
  <artifactId>camelbee-quarkus-core</artifactId>
  <version>latest-version</version>
</dependency>
```

## Configuration

### Configure your each Camel Route with org.camelbee.config.CamelBeeRouteConfigurer

To enable the interceptors of the CamelBee library configure your camel routes like below:

```

/**
 * Musician Route.
 *
 * @author ekaraosmanoglu
 */
@ApplicationScoped
public class MusicianRoute extends RouteBuilder {

	...
    ...

    @Override
    public void configure() throws Exception {

        camelBeeRouteConfigurer.configureRoute(this);

	...
    ...


```

### Enable CamelBee Features

To enable specific features of the CamelBee library, add/modify the following properties in your `application.yml` file:

```
camelbee:
  # When context-enabled is true, it allows the CamelBee WebGL application to fetch the topology of the Camel Context.
  context-enabled: true
  # When producer-enabled is true, it allows the CamelBee WebGL application to trigger the consumer routes.
  # producer-enabled SHOULD BE ONLY ENABLED FOR DEVELOPMENT AND TESTING PURPOSES, NOT FOR PRODUCTION.
  producer-enabled: true
  # When debugger-enabled is true, it intercepts and traces requests and responses of all Camel components and caches messages.
  # debugger-enabled SHOULD BE ONLY ENABLED FOR DEVELOPMENT AND TESTING PURPOSES, FOR PRODUCTION IT COULD BE USED TEMPORARILY.
  debugger-enabled: true
  # Maximum time the tracer can remain idle before deactivation tracing of messages.
  debugger-max-idle-time: 60000
```


### Enable Metrics and CORS for https://www.camelbee.io

To enable metrics and configure CORS for https://www.camelbee.io, adjust the following properties to your `application.yaml` file:

```
quarkus:
  http:
    port: 8080
    cors:
      ~: true
      origins: https://www.camelbee.io,http://localhost:8083
      methods: GET,POST,DELETE
      headers: "*"
      exposed-headers: Content-Disposition
      access-control-allow-credentials: true
      access-control-max-age: 24H
```


## Example Implementation

Discover a practical and functional application of this core library within the 'allcomponent-quarkus-sample' Maven project showcased below as a successful and operational example:

```shell
camelbee/
|-- camelbee-core/
| |-- camelbee-quarkus-core/
| | |-- ...
|-- camelbee-examples/
| |-- allcomponent-quarkus-sample/
| | |-- ...
```