package io.camelbee.springboot.example.exception;

import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Global Error Handler.
 *
 * @author ekaraosmanoglu
 */
@Component

public class ExceptionHandler extends RouteBuilder {

  final ErrorProcessor globalErrorProcessor;

  public ExceptionHandler(ErrorProcessor globalErrorProcessor) {
    this.globalErrorProcessor = globalErrorProcessor;
  }

  /**
   * The creates a new deadletter channel builder.
   */

  @Bean
  public DeadLetterChannelBuilder appErrorHandler() {
    var deadLetterChannelBuilder = new DeadLetterChannelBuilder();
    deadLetterChannelBuilder.setDeadLetterUri("direct:error");
    deadLetterChannelBuilder.logHandled(false);
    deadLetterChannelBuilder.useOriginalMessage();
    return deadLetterChannelBuilder;
  }

  /**
   * Configure global error route.
   *
   * @throws Exception can be thrown during configuration
   */
  @Override
  public void configure() throws Exception {

    CamelBeeRouteConfigurer.configure(this);

    from("direct:error").routeId("errorHandlerDirectComponent")
        .process(globalErrorProcessor);
  }
}
