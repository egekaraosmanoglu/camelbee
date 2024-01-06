# CamelBee SpringBoot Core Library

## Introduction

The camelbee-springboot-core library is an essential component for integrating a Camel SpringBoot Microservice with the CamelBee WebGL application (https://www.camelbee.io). 
This library provides the necessary functionalities to configure Camel routes with interceptors, allowing comprehensive tracing of messages exchanged between the routes. 
Additionally, it includes rest controllers for seamless interaction with the CamelBee WebGL application.

## Manual Installation

To manually install the core library, follow the steps below:

### Maven Installation

`mvn clean install`

Once the Maven artifact is created, you can include it in your project by adding the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>io.camelbee</groupId>
  <artifactId>camelbee-spring-core</artifactId>
  <version>latest-version</version>
</dependency>
```

## Configuration

Configure the following properties in your `application.yaml` file:

### Enable CamelBee Features

To enable specific features of the CamelBee library, add/modify the following properties:

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

To enable metrics and configure CORS for https://www.camelbee.io, adjust the following properties:

```
management:
  server:
    port: 8080
  security:
    enabled: false
  # expose actuator endpoint via HTTP for info,health,camelroutes
  endpoints:
    web:
      exposure:
        include: '*'
      base-path: /
      path-mapping:
        prometheus: metrics
        metrics: metrics-default
      cors:
        allowed-origins: "https://www.camelbee.io" # Comma-separated list of origins you want to allow
        allowed-methods: GET  # Allowed HTTP methods. Default to GET.
        allowed-headers: "*"  # Allowed HTTP headers. Default to "*".
        allow-credentials: true  # Set to true if you want to allow cookies, authorization headers, etc.
        max-age: 1800  # Maximum age (in seconds) of the cache duration for CORS preflight responses.
```


## Example Implementation

Discover a practical and functional application of this core library within the 'allcomponent-springboot-sample' Maven project showcased below as a successful and operational example:

```shell
camelbee/
|-- camelbee-core/
| |-- camelbee-springboot-core/
| | |-- ...
|-- camelbee-examples/
| |-- allcomponent-springboot-sample/
| | |-- ...
```